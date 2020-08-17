package io.nerv.core.token.util

import cn.hutool.core.util.StrUtil
import cn.hutool.extra.servlet.ServletUtil
import io.nerv.core.constant.CommonConstant
import io.nerv.core.token.jwt.JwtUtil
import io.nerv.core.util.RedisUtil
import io.nerv.core.util.RequestUtil
import io.nerv.properties.EvaConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.data.redis.cache.RedisCache
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest

@Component
class TokenUtil(cacheManager: CacheManager) {
    @Autowired
    private val evaConfig: EvaConfig? = null

    @Autowired
    private val requestUtil: RequestUtil? = null

    @Autowired
    private val jwtUtil: JwtUtil? = null

    @Autowired
    private val redisUtil: RedisUtil? = null

    private val tokenCache: Cache

    // 构造token缓存的value
    fun buildCacheValue(request: HttpServletRequest, uid: String?, token: String): Map<String, Any?> {
        return mapOf(Pair("device", requestUtil!!.getDeivce(request)),
               Pair("version", requestUtil.getVersion(request)),
               Pair("issuedAt", jwtUtil!!.getIssuedAt(token)),
               Pair("expireAt", jwtUtil.getExpirationDateFromToken(token)),
               Pair("loginTime", LocalDateTime.now()),
               Pair("account", uid),
               Pair("token", token))
    }

    // 持久化 token
    fun saveToken(key: String?, value: Any?) {
        tokenCache.put(key, value)
    }

    /***
     * 获取所有的token
     * @return
     */
    val allToken: Map<*, *>?
        get() {
            if (tokenCache is CaffeineCache) {
                return tokenCache.nativeCache.asMap()
            }
            return if (tokenCache is RedisCache) {
                redisUtil!!.getPureAll(CommonConstant.CACHE_TOKEN)
            } else null
        }

    /**
     * 获取token缓存
     * @param uid
     * @return
     */
    fun getToken(uid: String?): Any? {
        val wrapper = tokenCache[uid]
        return wrapper?.get()
    }

    /**
     * 从缓存中清除token
     * @param key
     */
    fun removeToken(key: String?) {
        tokenCache.evict(key)
    }

    /**
     * 获取access token
     * @param request
     * @return
     */
    fun getToken(request: HttpServletRequest): String? {
        return this.getToken(request, CommonConstant.ACCESS_TOKEN_KEY)
    }

    /**
     * 获取refresh token
     * @param request
     * @return
     */
    fun getRefreshToken(request: HttpServletRequest): String? {
        return this.getToken(request, CommonConstant.REFRESH_TOKEN_KEY)
    }

    /**
     * 获取token
     * @param request
     * @return
     */
    fun getToken(request: HttpServletRequest, tokenKey: String?): String? {
        var authToken: String? = null
        val authHeader = request.getHeader(evaConfig!!.jwt!!.header)
        if (null != ServletUtil.getCookie(request, CommonConstant.ACCESS_TOKEN_KEY)) {
            authToken = ServletUtil.getCookie(request, CommonConstant.ACCESS_TOKEN_KEY).value
        } else if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith(evaConfig.jwt!!.tokenHead)) {
            authToken = authHeader.substring(evaConfig.jwt!!.tokenHead.length)
        }
        return authToken
    }

    init {
        tokenCache = cacheManager.getCache(CommonConstant.CACHE_TOKEN)
    }
}