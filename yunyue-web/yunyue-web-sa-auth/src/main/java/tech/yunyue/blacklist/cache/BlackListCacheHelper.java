package tech.yunyue.blacklist.cache;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.stereotype.Component;
import tech.yunyue.core.cache.util.RedisUtil;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.util.json.JsonUtil;

import java.util.*;

/**
 * 黑名单信息缓存类
 */
@Data
@Component
public class BlackListCacheHelper {
    private Cache cache;
    private final RedisUtil redisUtil;

    @Autowired
    public BlackListCacheHelper(CacheManager cacheManager, RedisUtil redisUtil) {
        this.cache = cacheManager.getCache(CommonConstant.CACHE_BLACKDATA);
        this.redisUtil = redisUtil;
    }
    public List<String> getAll() {
        String jsonStr="";
        if (this.cache instanceof CaffeineCache) {
            CaffeineCache caffeineCache = (CaffeineCache) this.cache;
            jsonStr = (String) caffeineCache.getNativeCache().getIfPresent(CommonConstant.CACHE_BLACKDATA);
        }

        if (this.cache instanceof RedisCache) {
            jsonStr = cache.get(CommonConstant.CACHE_BLACKDATA,String.class);
        }
        return JsonUtil.parseArray(jsonStr, String.class);
    }

    /**
     * 新增黑名单 重新保存
     */
    public void add(String target) {
        List<String> list = getAll();
        list.add(target);
        cachePut(list);
    }
    /**
     * 修改黑名单 重新保存
     */
    public void add(String oldTarget, String newTarget) {
        List<String> list = getAll();
        list.remove(oldTarget);
        list.add(newTarget);
        cachePut(list);
    }

    /**
     * 删除一个黑名单 重新保存
     */
    public void remove(String target) {
        List<String> list = getAll();
        list.remove(target);
        cachePut(list);
    }

    public void removeAll() {
        this.cache.clear();
    }

    /**
     * 写入缓存
     */
    public void cachePut(List<String> list) {
        if(CollectionUtil.isEmpty(list)){
            list = CollectionUtil.newArrayList();
        }
        this.cache.put(CommonConstant.CACHE_BLACKDATA, JsonUtil.toJson(list));
    }
}
