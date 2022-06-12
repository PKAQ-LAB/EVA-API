package io.nerv.core.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.BizException;
import io.nerv.properties.EvaConfig;
import io.nerv.properties.Jwt;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

/**
 * JWT 工具类
 * @author: S.PKAQ
 */
@Data
@Component
@Slf4j
public class JwtUtil {
    @Autowired
    private EvaConfig evaConfig;

    public Jwt jwtConfig(){
        return this.evaConfig.getJwt();
    }

    /**
     * 生成密钥
     * @return
     */
    private byte[] generalKey() {
       return evaConfig.getJwt().getSecert().getBytes(StandardCharsets.UTF_8);
    }
    /**
     * 获取uid中的uid属性
     * @param token
     * @return
     */
    public String getUid(String token) {
        String uid = "";
        try {
            final JWTClaimsSet claims = getClaimsFromToken(token);
            uid = claims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uid;
    }
    /**
     * 获取jwt自定义属性
     * @param token jwt
     * @return 属性值
     */
    private JWTClaimsSet getClaimsFromToken(String token)  {
        // 解析token，将token转换为JWSObject对象
        JWSObject jwsObject = null;
        JWTClaimsSet jwtClaimsSet = null;
        try {
            jwsObject = JWSObject.parse(token);

            // 创建一个JSON Web Signature (JWS) verifier.用于校验签名(即:校验token是否被篡改)
            JWSVerifier jwsVerifier = new MACVerifier(this.generalKey());
            // 如果校验到token被篡改(即:签名认证失败)，那么抛出异常
            if(!jwsObject.verify(jwsVerifier)) {
                throw new BizException(BizCodeEnum.TOKEN_NOT_VERIFY);
            }
            jwtClaimsSet = JWTClaimsSet.parse(jwsObject.getPayload().toJSONObject());
        } catch (ParseException | JOSEException e) {
            e.printStackTrace();
        }

        return jwtClaimsSet;
    }

    /**
     *
     * @param ttlMillis 有效时间
     * @param uid   uid
     * @return jwt token
     */
    public String build(long ttlMillis, String uid)  {
        /**
         * 1.创建一个32-byte的密匙
         */
        MACSigner macSigner = null;
        try {
            macSigner = new MACSigner(this.generalKey());
        } catch (KeyLengthException e) {
            e.printStackTrace();
        }
        /**
         * 2. 建立payload 载体
         */

        long nowMillis = System.currentTimeMillis();

        // 过期时间
        long expMillis = nowMillis + (ttlMillis * 1000);
        Date exp =  new Date(expMillis);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issueTime(new Date(nowMillis))
                .issuer(this.jwtConfig().getSign())
                .subject(uid)
                .jwtID(uid)
                .expirationTime(ttlMillis > 0 ? exp : null)
                .notBeforeTime(new Date(nowMillis))
                .build();

        /**
         * 3. 建立签名
         */
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        try {
            signedJWT.sign(macSigner);
        } catch (JOSEException e) {
            e.printStackTrace();
        }

        /**
         * 4. 生成token
         */
        String token = signedJWT.serialize();
        return token;
    }
    /**
     * 验证token
     * @param jwtToken token串
     * @return 验证结果
     */
    public boolean valid(String jwtToken) throws BizException {
        boolean ret = false;
        // 解析token，将token转换为JWSObject对象
        JWSObject jwsObject = null;
        try {
            jwsObject = JWSObject.parse(jwtToken);
            // 创建一个JSON Web Signature (JWS) verifier.用于校验签名(即:校验token是否被篡改)
            JWSVerifier jwsVerifier = null;
            jwsVerifier = new MACVerifier(this.generalKey());
            // 如果校验到token被篡改(即:签名认证失败)，那么抛出异常
            if(!jwsObject.verify(jwsVerifier)) {
                throw new BizException(BizCodeEnum.TOKEN_NOT_VERIFY);
            }
            // 获取有效负载
            JWTClaimsSet claimsSet = JWTClaimsSet.parse(jwsObject.getPayload().toJSONObject());

            if (null == claimsSet || "-".equals(claimsSet.getSubject())){
                log.error("登录已失效");
                throw new BizException("登录已失效");
            } else {
                ret = true;
            }
        } catch (JOSEException | ParseException e) {
            e.printStackTrace();
        }
        return ret;
    }
    /**
     * 刷新TOKEN TOKEN續命
     * @param token
     * @return
     */
    public String refreshToken(String token) {
        final String uid = this.getUid(token);
        return this.build(this.jwtConfig().getThreshold(), uid);
    }

    /**
     * Token是否即将过期
     * @param token
     * @return
     */
    public Boolean isTokenExpiring(String token) throws BizException {
        Date expiration = null;
        expiration = getClaimsFromToken(token).getExpirationTime();
        return (expiration.getTime() - System.currentTimeMillis()) < (this.jwtConfig().getThreshold());
    }
    /**
     * 重新计算过期时间
     * @return
     */
    private Date calculateExpirationDate() {
        return new Date(System.currentTimeMillis() + this.jwtConfig().getTtl());

    }

    /**
     * 获取签发时间
     * @param token
     * @return
     */
    public Date getIssuedAt(String token){
        return this.getClaimsFromToken(token).getIssueTime();
    }

    /**
     * 获取过期时间
     * @param token
     * @return
     */
    public Date getExpirationDateFromToken(String token){
        return this.getClaimsFromToken(token).getExpirationTime();
    }
}
