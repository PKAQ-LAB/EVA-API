package io.nerv.sys.dict.cache;

import io.nerv.core.cache.util.RedisUtil;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.util.json.JsonUtil;
import lombok.Data;
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
 * @author: S.PKAQ
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

    public LinkedHashMap<String, String> get(String code) {
        Cache.ValueWrapper jsonStr = this.cache.get(code);
        return null != jsonStr ? JsonUtil.parse((String) jsonStr.get(), LinkedHashMap.class) : null;
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
        LinkedHashMap<String, String> itemMap = this.get(code);
        if (null != itemMap) {
            value = itemMap.get(key);
        }
        return value;
    }


    public void remove(String code) {
        this.cache.evict(code);
    }


    public void remove(String code, String key) {
        Map<String, String> itemMap = this.get(code);
        if (null != itemMap) {
            itemMap.remove(key);
        }
    }


    public void removeAll() {
        this.cache.clear();
    }


    public void update(String code, String key, String value) {
        Map<String, String> itemMap = this.get(code);
        if (null != itemMap) {
            itemMap.put(key, value);
            this.cachePut(code, itemMap);
        }
    }


    public void update(String code, LinkedHashMap<String, String> item) {
        this.cachePut(code, item);
    }


    public void add(String code, LinkedHashMap<String, String> item) {
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
        this.cache.put(k, JsonUtil.toJson(object));
    }
}
