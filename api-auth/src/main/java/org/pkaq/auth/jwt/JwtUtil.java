package org.pkaq.auth.jwt;

import cn.hutool.core.codec.Base64;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.pkaq.auth.exception.OathException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/4/20 15:16
 */
public class JwtUtil {

    private static SecretKey generalKey() {
        byte[] encodedKey = Base64.decode(JwtConstant.JWT_SECERT);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
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
                .setIssuer(JwtConstant.SIGN_USER)
                // 自定义属性
                .claim("userid", uid);

        // 设置过期时间,为0则永不过期
        if (ttlMillis > 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            // 失效时间
            jwt.setExpiration(exp);
        }
        return jwt.compact();
    }

    /**
     * 验证token
     * @param jwtToken token串
     * @return 验证结果
     */
    public boolean valid(String jwtToken){
        boolean valid = false;
        try {
            SecretKey secretKey = generalKey();
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(jwtToken)
                    .getBody();

            String uid = claims.get("uid", String.class);
            if ("-".equals(uid)) {
                throw new OathException("登录已失效");
            }
            valid = true;
        } catch (SignatureException se ) {
            //在解析JWT字符串时，如果密钥不正确，将会解析失败，抛出SignatureException异常，说明该JWT字符串是伪造的
            System.out.println("密钥错误");
        } catch (ExpiredJwtException ee) {
            //在解析JWT字符串时，如果‘过期时间字段’已经早于当前时间，将会抛出ExpiredJwtException异常，说明本次请求已经失效
            System.out.println("token过时");
        } catch (OathException oe) {
            System.out.println("登录已失效");
        }

        return valid;
    }
}
