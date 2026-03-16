package org.pkaq.core.auth.openapi.security;

import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.auth.AuthCodes;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * 签名校验器
 *
 * @author PKAQ
 */
@Slf4j
@Component
public class SignatureValidator {

    private final EvaConfig evaConfig;

    public SignatureValidator(EvaConfig evaConfig) {
        this.evaConfig = evaConfig;
    }

    /**
     * 验证签名
     *
     * @param appKey AppKey
     * @param appSecret AppSecret
     * @param timestamp 时间戳(秒)
     * @param requestPath 请求路径
     * @param requestBody 请求体
     * @param signature 客户端签名
     * @return 是否验证通过
     */
    public boolean validateSignature(String appKey, String appSecret,
                                     long timestamp, String requestPath,
                                     String requestBody, String signature) {
        if (!validateTimestamp(timestamp)) {
            long currentTimestamp = Instant.now().getEpochSecond();
            long diff = Math.abs(currentTimestamp - timestamp);
            log.warn("无效的时间戳 - 提供: {}, 当前: {}, 差异: {}s, 容忍度: {}s",
                    timestamp, currentTimestamp, diff, getTimestampToleranceSeconds());
            AuthCodes.OPENAPI_TIMESTAMP_OUT_OF_TOLERANCE.newException();
        }

        String serverSignature;
        try {
            serverSignature = generateSignature(appKey, appSecret, timestamp, requestPath, requestBody);
        } catch (Exception e) {
            log.error("生成签名失败", e);
            AuthCodes.OPENAPI_SIGNATURE_GENERATION_FAILED.newException();
            return false;
        }

        boolean valid = serverSignature.equalsIgnoreCase(signature);
        if (!valid) {
            log.warn("签名不匹配 - AppKey: {}, 路径: {}", appKey, requestPath);
            if (log.isDebugEnabled()) {
                log.debug("期望签名: {}, 实际签名: {}", serverSignature, signature);
            }
            AuthCodes.OPENAPI_INVALID_SIGNATURE.newException();
        } else {
            log.debug("签名验证通过 - AppKey: {}, 路径: {}", appKey, requestPath);
        }

        return valid;
    }

    /**
     * 生成签名
     *
     * @param appKey AppKey
     * @param appSecret AppSecret
     * @param timestamp 时间戳(秒)
     * @param requestPath 请求路径
     * @param requestBody 请求体
     * @return 签名字符串(十六进制)
     * @throws NoSuchAlgorithmException 算法不存在
     * @throws InvalidKeyException 密钥无效
     */
    public String generateSignature(String appKey, String appSecret,
                                    long timestamp, String requestPath,
                                    String requestBody) throws NoSuchAlgorithmException, InvalidKeyException {
        String signContent = buildSignContent(appKey, timestamp, requestPath, requestBody);
        log.info("签名内容: {}", signContent);

        String algorithm = evaConfig.getAuth().getSignatureAlgorithm();
        Mac mac = Mac.getInstance(algorithm);
        SecretKeySpec secretKey = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), algorithm);
        mac.init(secretKey);

        byte[] hmacBytes = mac.doFinal(signContent.getBytes(StandardCharsets.UTF_8));
        return toHex(hmacBytes);
    }

    /**
     * 构建待签名内容
     * 格式: AppKey + Timestamp + RequestPath + RequestBody
     */
    private String buildSignContent(String appKey, long timestamp, String requestPath, String requestBody) {
        StringBuilder sb = new StringBuilder();
        sb.append(appKey);
        sb.append(timestamp);
        sb.append(requestPath);
        if (requestBody != null && !requestBody.isEmpty()) {
            sb.append(requestBody);
        }
        return sb.toString();
    }

    /**
     * 验证时间戳有效性
     */
    private boolean validateTimestamp(long timestamp) {
        long currentTimestamp = Instant.now().getEpochSecond();
        long diff = Math.abs(currentTimestamp - timestamp);

        boolean valid = diff <= getTimestampToleranceSeconds();
        if (!valid && log.isDebugEnabled()) {
            log.debug("时间戳超出容忍度 - 差异: {}s, 容忍度: {}s", diff, getTimestampToleranceSeconds());
        }

        return valid;
    }

    private long getTimestampToleranceSeconds() {
        return evaConfig.getAuth().getSignatureTimestampToleranceSeconds();
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String toHex(byte[] bytes) {
        char[] hexChars = "0123456789abcdef".toCharArray();
        char[] result = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            result[i * 2] = hexChars[value >>> 4];
            result[i * 2 + 1] = hexChars[value & 0x0F];
        }
        return new String(result);
    }
}
