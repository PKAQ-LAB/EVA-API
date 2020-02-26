package io.nerv.core.token.jwt;

import cn.hutool.core.codec.Base64;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClock;
import io.nerv.core.exception.OathException;
import io.nerv.properties.EvaConfig;
import io.nerv.properties.Jwt;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT 工具类
 * @author: S.PKAQ
 * @Datetime: 2018/4/20 15:16
 */
@Data
@Component
@Slf4j
public class JwtUtil {
    @Autowired
    private EvaConfig evaConfig;

    private Clock clock = DefaultClock.INSTANCE;

    public Jwt jwtConfig(){
        return this.evaConfig.getJwt();
    }

    /**
     * 生成加密K
     * @return
     */
    private SecretKey generalKey() {
        byte[] encodedKey = Base64.decode(evaConfig.getJwt().getSecert());
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }
    /**
     * 获取claim
     * @param token
     * @param claimsResolver
     * @param <T>
     * @return
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    /**
     * 获取uid中的uid属性
     * @param token
     * @return
     */
    public String getUid(String token) {
        String uid = "";
        try {
            final Claims claims = getClaimsFromToken(token);
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
    private Claims getClaimsFromToken(String token) {
        SecretKey secretKey = generalKey();
        Claims claims = null;
        try {
            claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        }catch (ExpiredJwtException e){
            log.error("token已过期", e);
            throw new OathException("Token 已过期");
        }finally {
            return claims;
        }
    }
    /**
     *
     * @param ttlMillis 有效时间
     * @param uid   uid
     * @return jwt token
     */
    public String build(long ttlMillis, String uid){
        SecretKey secretKey = generalKey();

        long nowMillis = System.currentTimeMillis();
        JwtBuilder jwt = Jwts.builder()
                // JWT_ID
                .setId(uid)
                // 签名算法和密钥
                .signWith(SignatureAlgorithm.HS256, secretKey)
                // 签发时间
                .setIssuedAt(new Date(nowMillis))
                // 签发人
                .setIssuer(this.jwtConfig().getSign())
                // 主题
                .setSubject(uid);

        // 设置过期时间,为0则永不过期
        if (ttlMillis > 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            // 失效时间
            jwt.setExpiration(exp).setNotBefore(new Date(System.currentTimeMillis()));
        }
        return jwt.compact();
    }
    /**
     * 验证token
     * @param jwtToken token串
     * @return 验证结果
     */
    public boolean valid(String jwtToken) throws OathException {
        boolean ret;
        Claims claims = this.getClaimsFromToken(jwtToken);

        if (null == claims || "-".equals(claims.getSubject())) {
            log.error("登录已失效");
            throw new OathException("登录已失效");
        } else {
            ret = true;
        }

        return ret;
    }
    /**
     * 刷新TOKEN TOKEN續命
     * @param token
     * @return
     */
    public String refreshToken(String token) {
        final Date createdDate = clock.now();
        final Date expirationDate = calculateExpirationDate(createdDate);

        final Claims claims = getClaimsFromToken(token);
        claims.setIssuedAt(createdDate);
        claims.setExpiration(expirationDate);

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, this.jwtConfig().getSecert())
                .compact();
    }
    /**
     * Token是否过期
     * @param token
     * @return
     */
    public Boolean isTokenExpired(String token) throws OathException {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(clock.now());
    }

    /**
     * Token是否即将过期
     * @param token
     * @return
     */
    public Boolean isTokenExpiring(String token) throws OathException {
        Date expiration = getExpirationDateFromToken(token);
        return (expiration.getTime() - clock.now().getTime()) < (this.jwtConfig().getThreshold());
    }
    /**
     * 重新计算过期时间
     * @param createdDate
     * @return
     */
    private Date calculateExpirationDate(Date createdDate) {
        return new Date(clock.now().getTime() + this.jwtConfig().getTtl());

    }
    /**
     * 从token中获取过期时间
     * @param token
     * @return
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    /**
     * 从tokne中获取签发时间
     * @param token
     * @return
     */
    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }
}
