package org.pkaq.core.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import org.pkaq.core.cache.util.RedisUtil;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.web.core.utils.RequestUtil;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author PKAQ
 */
@Component
public class CacheTokenUtil {

    private final JwtUtil jwtUtil;

    private final RedisUtil redisUtil;

    private Cache tokenCache;

    public CacheTokenUtil(JwtUtil jwtUtil, RedisUtil redisUtil, CacheManager cacheManager) {
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
        this.tokenCache = cacheManager.getCache(CommonConstant.CACHE_TOKEN);
    }

    /**
     * 构造token缓存的value
     */
    public Map<String, Object> buildCacheValue(HttpServletRequest request, Long uid, String token) {
        return Map.of("device", RequestUtil.getDeivce(request),
                "version", RequestUtil.getVersion(request),
                "issuedAt", jwtUtil.getIssuedAt(token),
                "expireAt", jwtUtil.getExpirationDateFromToken(token),
                "loginTime", LocalDateTime.now(),
                "account", uid,
                "token", token);
    }

    /**
     * 持久化 token
     *
     * @param key
     * @param value
     */
    public void saveToken(Long key, Object value) {
        this.tokenCache.put(key, value);
    }


    /***
     * 获取所有的token
     * @return
     */
    public Map<?, ?> getAllToken() {
        if (this.tokenCache instanceof CaffeineCache) {
            CaffeineCache caffeineCache = (CaffeineCache) this.tokenCache;
            return caffeineCache.getNativeCache().asMap();
        }

        if (this.tokenCache instanceof RedisCache) {
            return redisUtil.getPureAll(CommonConstant.CACHE_TOKEN);
        }
        return null;
    }

    /**
     * 获取token缓存
     *
     * @param uid
     * @return
     */
    public Object getToken(Long uid) {
        var wrapper = this.tokenCache.get(uid);
        return null == wrapper ? null : wrapper.get();
    }

    /**
     * 从缓存中清除token
     *
     * @param key
     */
    public void removeToken(String key) {
        this.tokenCache.evict(key);
    }

}
