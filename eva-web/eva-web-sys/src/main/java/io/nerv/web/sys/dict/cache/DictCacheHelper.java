package io.nerv.web.sys.dict.cache;

import io.nerv.core.constant.CommonConstant;
import io.nerv.core.util.JsonUtil;
import io.nerv.core.util.RedisUtil;
import io.nerv.web.sys.dict.service.DictService;
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
 * @author: S.PKAQ
 * @Datetime: 2019/3/15 13:18
 */
@Data
@Component
public class DictCacheHelper {

    @Autowired
    private JsonUtil jsonUtil;

    private Cache cache;

    @Autowired
    private DictService dictService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    public DictCacheHelper(CacheManager cacheManager) {
        this.cache = cacheManager.getCache(CommonConstant.CACHE_DICTDATA);
    }

    /**
     * 初始化字典数据
     */
   
    public void init() {
        var dictMap = dictService.initDictCache();
        dictMap.forEach((k, v) ->
            this.cachePut(k, v)
        );
    }

    /**
     * 根据传入的内容重新初始化字典
     * @param dictMap
     */
   
    public void init(Map<String, LinkedHashMap<String, String>> dictMap) {
        dictMap.forEach((k, v) ->
            this.cachePut(k, v)
        );
    }

   
    public Map<?, ?> getAll() {
        if (this.cache instanceof CaffeineCache){
            CaffeineCache caffeineCache = (CaffeineCache)this.cache;
            return caffeineCache.getNativeCache().asMap();
        }

        if (this.cache instanceof RedisCache){
            return redisUtil.getPureAll(CommonConstant.CACHE_DICTDATA);
        }

        return null;
    }

    /**
     * 根据code查找值
     * @param code
     * @return
     */
   
    public LinkedHashMap<String, String> get(String code){
        Cache.ValueWrapper jsonStr = this.cache.get(code);
        return null != jsonStr? jsonUtil.parseObject((String) jsonStr.get(), LinkedHashMap.class): null;
    }

    /**
     * 获取字典项值
     * @param code
     * @param key
     * @return
     */
   
    public String get(String code, String key) {
        String value = null;
        LinkedHashMap<String, String> itemMap = this.get(code);
        if (null != itemMap){
            value = itemMap.get(key);
        }
        return value;
    }

   
    public void remove(String code) {
        this.cache.evict(code);
    }

   
    public void remove(String code, String key) {
        Map<String, String> itemMap = this.get(code);
        if (null != itemMap){
            itemMap.remove(key);
        }
    }

   
    public void removeAll() {
        this.cache.clear();
    }

   
    public void update(String code, String key, String value) {
        Map<String, String> itemMap = this.get(code);
        if (null != itemMap){
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

   
    public void reload() {
        this.removeAll();
        this.init();
    }

   
    public void reload(Map<String, LinkedHashMap<String, String>> dictMap) {
        this.removeAll();
        this.init(dictMap);
    }

    /**
     * 写入缓存
     * @param k
     * @param object
     */
    private void cachePut(String k, Object object){
        this.cache.put(k, jsonUtil.toJSONString(object));
    }
}
