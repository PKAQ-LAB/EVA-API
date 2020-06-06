package io.nerv.core.token.jwt

import cn.hutool.core.codec.Base64
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.DefaultClock
import io.nerv.exception.OathException
import io.nerv.properties.EvaConfig
import io.nerv.properties.Jwt
import lombok.Data
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import java.util.function.Function
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * JWT 工具类
 * @author: S.PKAQ
 * @Datetime: 2018/4/20 15:16
 */
@Data
@Component
@Slf4j
class JwtUtil {
    @Autowired
    private val evaConfig: EvaConfig? = null
    private val clock = DefaultClock.INSTANCE
    fun jwtConfig(): Jwt? {
        return evaConfig!!.jwt
    }

    /**
     * 生成加密K
     * @return
     */
    private fun generalKey(): SecretKey {
        val encodedKey = Base64.decode(evaConfig!!.jwt!!.secert)
        return SecretKeySpec(encodedKey, 0, encodedKey.size, "AES")
    }

    /**
     * 获取claim
     * @param token
     * @param claimsResolver
     * @param <T>
     * @return
    </T> */
    fun <T> getClaimFromToken(token: String, claimsResolver: Function<Claims?, T>): T {
        val claims = getClaimsFromToken(token)
        return claimsResolver.apply(claims)
    }

    /**
     * 获取uid中的uid属性
     * @param token
     * @return
     */
    fun getUid(token: String): String {
        var uid = ""
        try {
            val claims = getClaimsFromToken(token)
            uid = claims!!.subject
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return uid
    }

    /**
     * 获取jwt自定义属性
     * @param token jwt
     * @return 属性值
     */
    private fun getClaimsFromToken(token: String): Claims? {
        val secretKey = generalKey()
        var claims: Claims? = null
        claims = try {
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .body
        } catch (e: ExpiredJwtException) {
            JwtUtil.log.error("token已过期", e)
            throw OathException("Token 已过期")
        } finally {
            return claims
        }
    }

    /**
     * 获取token的签发时间
     * @param token
     * @return
     */
    fun getIssuedAt(token: String): Date {
        return getClaimFromToken(token, Function { obj: Claims? -> obj!!.issuedAt })
    }

    /**
     *
     * @param ttlMillis 有效时间
     * @param uid   uid
     * @return jwt token
     */
    fun build(ttlMillis: Long, uid: String?): String {
        val secretKey = generalKey()
        val nowMillis = System.currentTimeMillis()
        val jwt = Jwts.builder() // JWT_ID
                .setId(uid) // 签名算法和密钥
                .signWith(SignatureAlgorithm.HS256, secretKey) // 签发时间
                .setIssuedAt(Date(nowMillis)) // 签发人
                .setIssuer(jwtConfig()!!.sign) // 主题
                .setSubject(uid)

        // 设置过期时间,为0则永不过期
        if (ttlMillis > 0) {
            val expMillis = nowMillis + ttlMillis * 1000
            val exp = Date(expMillis)
            // 失效时间
            jwt.setExpiration(exp).setNotBefore(Date(System.currentTimeMillis()))
        }
        return jwt.compact()
    }

    /**
     * 验证token
     * @param jwtToken token串
     * @return 验证结果
     */
    @Throws(OathException::class)
    fun valid(jwtToken: String): Boolean {
        val ret: Boolean
        val claims = getClaimsFromToken(jwtToken)
        ret = if (null == claims || "-" == claims.subject) {
            JwtUtil.log.error("登录已失效")
            throw OathException("登录已失效")
        } else {
            true
        }
        return ret
    }

    /**
     * 刷新TOKEN TOKEN續命
     * @param token
     * @return
     */
    fun refreshToken(token: String): String {
        val createdDate = clock.now()
        val expirationDate = calculateExpirationDate(createdDate)
        val claims = getClaimsFromToken(token)
        claims!!.issuedAt = createdDate
        claims.expiration = expirationDate
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, jwtConfig()!!.secert)
                .compact()
    }

    /**
     * Token是否过期
     * @param token
     * @return
     */
    @Throws(OathException::class)
    fun isTokenExpired(token: String): Boolean {
        val expiration = getExpirationDateFromToken(token)
        return expiration.before(clock.now())
    }

    /**
     * Token是否即将过期
     * @param token
     * @return
     */
    @Throws(OathException::class)
    fun isTokenExpiring(token: String): Boolean {
        val expiration = getExpirationDateFromToken(token)
        return expiration.time - clock.now().time < jwtConfig()!!.threshold
    }

    /**
     * 重新计算过期时间
     * @param createdDate
     * @return
     */
    private fun calculateExpirationDate(createdDate: Date): Date {
        return Date(clock.now().time + jwtConfig()!!.ttl)
    }

    /**
     * 从token中获取过期时间
     * @param token
     * @return
     */
    fun getExpirationDateFromToken(token: String): Date {
        return getClaimFromToken(token, Function { obj: Claims? -> obj!!.expiration })
    }

    /**
     * 从tokne中获取签发时间
     * @param token
     * @return
     */
    fun getIssuedAtDateFromToken(token: String): Date {
        return getClaimFromToken(token, Function { obj: Claims? -> obj!!.issuedAt })
    }
}