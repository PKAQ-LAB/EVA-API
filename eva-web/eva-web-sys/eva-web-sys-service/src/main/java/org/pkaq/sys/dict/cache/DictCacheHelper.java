package org.pkaq.sys.dict.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import org.pkaq.core.cache.util.RedisUtil;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.util.json.JsonUtil;
import org.pkaq.core.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 初始化字典信息
 *
 * @author PKAQ
 */
@Data
@Component
public class DictCacheHelper {
    private final RedisUtil redisUtil;
    private Cache cache;

    @Autowired
    public DictCacheHelper(CacheManager cacheManager, RedisUtil redisUtil) {
        this.cache = cacheManager.getCache(CommonConstant.CACHE_DICTDATA);
        this.redisUtil = redisUtil;
    }

    public Map<?, ?> getAll() {
        Map<?, ?> all = rawAll();
        String schema = TenantContext.schemaName();
        if (schema == null || all == null) {
            return all;
        }
        String prefix = schema + "::";
        Map<String, Object> scoped = new LinkedHashMap<>();
        all.forEach((key, value) -> {
            String cacheKey = String.valueOf(key);
            if (cacheKey.startsWith(prefix)) {
                scoped.put(cacheKey.substring(prefix.length()), value);
            }
        });
        return scoped;
    }

    private Map<?, ?> rawAll() {
        if (this.cache instanceof CaffeineCache) {
            CaffeineCache caffeineCache = (CaffeineCache) this.cache;
            return caffeineCache.getNativeCache().asMap();
        }

        if (this.cache instanceof RedisCache) {
            return redisUtil.getPureAll(CommonConstant.CACHE_DICTDATA);
        }

        return null;
    }

    /**
     * 根据code查找值
     *
     * @param code
     * @return
     */

    public Map<String, String> get(String code) {
        Cache.ValueWrapper jsonStr = this.cache.get(scopedKey(code));
        return null != jsonStr ? JsonUtil.parse((String) jsonStr.get(), LinkedHashMap.class) : null;
    }

    public <T> T getObject(String key, TypeReference<T> typeReference) {
        Cache.ValueWrapper jsonStr = this.cache.get(scopedKey(key));
        return null != jsonStr ? JsonUtil.parse((String) jsonStr.get(), typeReference) : null;
    }

    /**
     * 获取字典项值
     *
     * @param code
     * @param key
     * @return
     */

    public String get(String code, String key) {
        String value = null;
        Map<String, String> itemMap = this.get(code);
        if (null != itemMap) {
            value = itemMap.get(key);
        }
        return value;
    }


    public void remove(String code) {
        this.cache.evict(scopedKey(code));
    }


    public void remove(String code, String key) {
        Map<String, String> itemMap = this.get(code);
        if (null != itemMap) {
            itemMap.remove(key);
        }
    }


    public void removeAll() {
        String schema = TenantContext.schemaName();
        if (schema == null) {
            this.cache.clear();
            return;
        }
        rawAll().keySet().stream()
                .map(String::valueOf)
                .filter(key -> key.startsWith(schema + "::"))
                .forEach(this.cache::evict);
    }


    public void update(String code, String key, String value) {
        Map<String, String> itemMap = this.get(code);
        if (null != itemMap) {
            itemMap.put(key, value);
            this.cachePut(code, itemMap);
        }
    }


    public void update(String code, Map<String, String> item) {
        this.cachePut(code, item);
    }


    public void add(String code, Map<String, String> item) {
        this.cachePut(code, item);
    }


    public void add(String code, String key, String value) {
        this.update(code, key, value);
    }


    /**
     * 写入缓存
     *
     * @param k
     * @param object
     */
    public void cachePut(String k, Object object) {
        this.cache.put(scopedKey(k), JsonUtil.toJson(object));
    }

    private String scopedKey(String key) {
        String schema = TenantContext.schemaName();
        return schema == null ? key : schema + "::" + key;
    }
}
