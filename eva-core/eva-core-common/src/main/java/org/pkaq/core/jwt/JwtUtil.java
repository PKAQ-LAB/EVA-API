package org.pkaq.core.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.Jwt;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * JWT 工具类
 *
 * @author PKAQ
 */
@Data
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {
    private final EvaConfig evaConfig;

    private static final String CLAIM_UID = "uid";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERM_VER = "permVer";
    private static final String CLAIM_TOKEN_TYPE = "typ";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    public Jwt jwtConfig() {
        return this.evaConfig.getJwt();
    }

    /**
     * 生成密钥
     */
    private byte[] generalKey() {
        return evaConfig.getJwt().getSecert().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 获取用户ID
     */
    public long getUid(String token) {
        try {
            final JWTClaimsSet claims = getClaimsFromToken(token);
            if (claims == null) {
                return 0;
            }
            Object uidClaim = claims.getClaim(CLAIM_UID);
            if (uidClaim instanceof Number num) {
                return num.longValue();
            }
            if (uidClaim != null) {
                return Long.parseLong(uidClaim.toString());
            }
            String jti = claims.getJWTID();
            if (jti != null) {
                return Long.parseLong(jti);
            }
        } catch (Exception e) {
            log.warn("获取用户ID失败", e);
        }
        return 0;
    }

    /**
     * 获取用户账号
     */
    public String getAccount(String token) {
        String uid = "";
        try {
            final JWTClaimsSet claims = getClaimsFromToken(token);
            if (claims != null) {
                uid = claims.getSubject();
            }
        } catch (Exception e) {
            log.warn("获取用户账号失败", e);
        }
        return uid;
    }

    /**
     * 从Token中获取角色ID列表
     */
    @SuppressWarnings("unchecked")
    public List<Long> getRoles(String token) {
        try {
            final JWTClaimsSet claims = getClaimsFromToken(token);
            if (claims == null) {
                return Collections.emptyList();
            }
            List<Object> raw = (List<Object>) claims.getClaim(CLAIM_ROLES);
            if (raw != null) {
                return raw.stream().map(o -> ((Number) o).longValue()).toList();
            }
        } catch (Exception e) {
            log.warn("从Token中获取角色失败", e);
        }
        return Collections.emptyList();
    }

    /**
     * 从Token中获取权限版本号
     */
    public long getPermVer(String token) {
        try {
            final JWTClaimsSet claims = getClaimsFromToken(token);
            if (claims == null) {
                return 0;
            }
            Object val = claims.getClaim(CLAIM_PERM_VER);
            if (val instanceof Number num) {
                return num.longValue();
            }
        } catch (Exception e) {
            log.warn("从Token中获取permVer失败", e);
        }
        return 0;
    }

    /**
     * 获取jwt自定义属性
     *
     * @param token jwt
     * @return 属性值
     */
    private JWTClaimsSet getClaimsFromToken(String token) {
        JWSObject jwsObject;
        JWTClaimsSet jwtClaimsSet = null;
        try {
            jwsObject = JWSObject.parse(token);

            JWSVerifier jwsVerifier = new MACVerifier(this.generalKey());
            if (!jwsObject.verify(jwsVerifier)) {
                log.warn("Token签名校验失败");
                return null;
            }
            jwtClaimsSet = JWTClaimsSet.parse(jwsObject.getPayload().toJSONObject());
        } catch (ParseException | JOSEException e) {
            log.warn("解析Token失败", e);
        }

        return jwtClaimsSet;
    }

    /**
     * 构建JWT（兼容旧版调用）
     *
     * @param ttlMillis 有效时间，单位毫秒
     * @param username  username
     * @return jwt token
     */
    public String build(long ttlMillis, long userId, String username) {
        return build(ttlMillis, userId, username, null, 0, TOKEN_TYPE_ACCESS);
    }

    /**
     * 构建JWT（含角色和权限版本）
     *
     * @param ttlMillis 有效时间，单位毫秒
     * @param userId    用户ID
     * @param username  用户名
     * @param roleIds   角色ID列表
     * @param permVer   权限版本号
     * @return jwt token
     */
    public String build(long ttlMillis, long userId, String username, List<Long> roleIds, long permVer) {
        return build(ttlMillis, userId, username, roleIds, permVer, TOKEN_TYPE_ACCESS);
    }

    /**
     * 构建刷新Token。
     *
     * @param ttlMillis 有效时间，单位毫秒
     * @param userId    用户ID
     * @param username  用户名
     * @param roleIds   角色ID列表
     * @param permVer   权限版本号
     * @return refresh token
     */
    public String buildRefreshToken(long ttlMillis, long userId, String username, List<Long> roleIds, long permVer) {
        return build(ttlMillis, userId, username, roleIds, permVer, TOKEN_TYPE_REFRESH);
    }

    /**
     * 构建指定类型的JWT。
     */
    private String build(long ttlMillis,
                         long userId,
                         String username,
                         List<Long> roleIds,
                         long permVer,
                         String tokenType) {
        MACSigner macSigner = null;
        try {
            macSigner = new MACSigner(this.generalKey());
        } catch (KeyLengthException e) {
            log.warn("生成Token失败", e);
        }

        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + ttlMillis;
        Date exp = new Date(expMillis);

        var builder = new JWTClaimsSet.Builder()
                .issueTime(new Date(nowMillis))
                .issuer(this.jwtConfig().getSign())
                .subject(username)
                .jwtID(String.valueOf(userId))
                .claim(CLAIM_UID, userId)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .expirationTime(ttlMillis > 0 ? exp : null)
                .notBeforeTime(new Date(nowMillis));

        if (roleIds != null && !roleIds.isEmpty()) {
            builder.claim(CLAIM_ROLES, roleIds);
        }
        if (permVer >= 0) {
            builder.claim(CLAIM_PERM_VER, permVer);
        }

        JWTClaimsSet claimsSet = builder.build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        try {
            signedJWT.sign(macSigner);
        } catch (JOSEException e) {
            log.warn("签名Token失败", e);
        }

        return signedJWT.serialize();
    }

    /**
     * 验证token
     *
     * @param jwtToken token值
     * @return 验证结果
     */
    public boolean valid(String jwtToken) throws BizException {
        if (jwtToken == null || jwtToken.isBlank()) {
            return false;
        }
        try {
            JWSObject jwsObject = JWSObject.parse(jwtToken);
            JWSVerifier jwsVerifier = new MACVerifier(this.generalKey());
            if (!jwsObject.verify(jwsVerifier)) {
                return false;
            }
            JWTClaimsSet claimsSet = JWTClaimsSet.parse(jwsObject.getPayload().toJSONObject());
            if (claimsSet == null || claimsSet.getSubject() == null || "-".equals(claimsSet.getSubject())) {
                return false;
            }

            Date now = new Date();
            Date notBefore = claimsSet.getNotBeforeTime();
            Date expiration = claimsSet.getExpirationTime();
            if (notBefore != null && notBefore.after(now)) {
                return false;
            }
            if (expiration == null || !expiration.after(now)) {
                return false;
            }
            return this.jwtConfig().getSign().equals(claimsSet.getIssuer());
        } catch (JOSEException | ParseException e) {
            log.warn("验证Token失败", e);
        }
        return false;
    }

    /**
     * 判断是否为access token。
     */
    public boolean isAccessToken(String token) {
        return TOKEN_TYPE_ACCESS.equals(getTokenType(token));
    }

    /**
     * 判断是否为refresh token。
     */
    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(getTokenType(token));
    }

    /**
     * 获取Token类型。
     */
    private String getTokenType(String token) {
        JWTClaimsSet claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        Object tokenType = claims.getClaim(CLAIM_TOKEN_TYPE);
        return tokenType == null ? null : tokenType.toString();
    }

    /**
     * 刷新TOKEN（保留roles和permVer）
     */
    public String refreshToken(String token) {
        final long uid = this.getUid(token);
        final String account = this.getAccount(token);
        final List<Long> roles = this.getRoles(token);
        final long permVer = this.getPermVer(token);
        return this.build(this.jwtConfig().getAlphaTtl(), uid, account, roles, permVer);
    }

    /**
     * Token是否即将过期
     */
    public Boolean isTokenExpiring(String token) throws BizException {
        JWTClaimsSet claims = getClaimsFromToken(token);
        if (claims == null || claims.getExpirationTime() == null) {
            return false;
        }
        Date expiration = claims.getExpirationTime();
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return remaining > 0 && remaining < this.jwtConfig().getThreshold();
    }

    /**
     * 重新计算过期时间
     */
    private Date calculateExpirationDate() {
        return new Date(System.currentTimeMillis() + this.jwtConfig().getTtl());
    }

    /**
     * 获取签发时间
     */
    public Date getIssuedAt(String token) {
        JWTClaimsSet claims = this.getClaimsFromToken(token);
        return claims == null ? null : claims.getIssueTime();
    }

    /**
     * 获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        JWTClaimsSet claims = this.getClaimsFromToken(token);
        return claims == null ? null : claims.getExpirationTime();
    }
}
