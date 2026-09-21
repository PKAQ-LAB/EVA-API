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
    public void saveToken(Long tenantId, Long userId, Object value) {
        this.tokenCache.put(cacheKey(tenantId, userId), value);
    }

    public void saveToken(Long userId, Object value) {
        saveToken(0L, userId, value);
    }


    /***
     * 获取所有的token
     * @return
     */
    public Map<?, ?> getAllToken() {
        return getTokens(null);
    }

    /**
     * 按可信租户范围读取在线会话。
     *
     * @param tenantId 租户ID；null 表示平台全局范围
     * @return 逻辑缓存键到会话元数据的映射
     */
    public Map<?, ?> getTokens(Long tenantId) {
        String entryPrefix = tenantId == null ? "" : tenantId + ":";
        if (this.tokenCache instanceof CaffeineCache) {
            CaffeineCache caffeineCache = (CaffeineCache) this.tokenCache;
            return caffeineCache.getNativeCache().asMap().entrySet().stream()
                    .filter(entry -> String.valueOf(entry.getKey()).startsWith(entryPrefix))
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue,
                            (first, second) -> first, java.util.LinkedHashMap::new));
        }

        if (this.tokenCache instanceof RedisCache) {
            return redisUtil.scanPureAll(CommonConstant.CACHE_TOKEN, entryPrefix);
        }
        return null;
    }

    /**
     * 获取token缓存
     *
     * @param uid
     * @return
     */
    public Object getToken(Long tenantId, Long userId) {
        var wrapper = this.tokenCache.get(cacheKey(tenantId, userId));
        return null == wrapper ? null : wrapper.get();
    }

    public Object getToken(Long userId) {
        return getToken(0L, userId);
    }

    /**
     * 从缓存中清除token
     *
     * @param key
     */
    public void removeToken(String key) {
        this.tokenCache.evict(key);
    }

    /**
     * 从缓存中按用户 ID 清除 token（与 saveToken(Long) 配对）
     * 用于冻结用户 / 冻结租户下用户时立即踢下线
     *
     * @param uid 用户 ID
     */
    public void removeToken(Long uid) {
        removeToken(0L, uid);
    }

    public void removeToken(Long tenantId, Long userId) {
        if (userId != null) {
            this.tokenCache.evict(cacheKey(tenantId, userId));
        }
    }

    /**
     * 批量按用户 ID 清除 token
     *
     * @param uids 用户 ID 集合
     */
    public void removeTokens(java.util.Collection<Long> uids) {
        removeTokens(0L, uids);
    }

    public void removeTokens(Long tenantId, java.util.Collection<Long> uids) {
        if (uids == null || uids.isEmpty()) {
            return;
        }
        for (Long uid : uids) {
            removeToken(tenantId, uid);
        }
    }

    public void removeTenantTokens(Long tenantId) {
        String prefix = (tenantId == null ? 0L : tenantId) + ":";
        Map<?, ?> tokens = getAllToken();
        if (tokens == null) {
            return;
        }
        tokens.keySet().stream()
                .map(String::valueOf)
                .filter(key -> key.startsWith(prefix))
                .forEach(this.tokenCache::evict);
    }

    private String cacheKey(Long tenantId, Long userId) {
        long trustedTenantId = tenantId == null ? 0L : tenantId;
        return trustedTenantId + ":" + userId;
    }

}
