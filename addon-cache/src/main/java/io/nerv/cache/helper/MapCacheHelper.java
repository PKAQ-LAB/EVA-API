package io.nerv.cache.helper;

import cn.hutool.cache.Cache;
import cn.hutool.cache.impl.FIFOCache;
import io.nerv.cache.condition.DefaultCacheCondition;
import io.nerv.config.CacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * Map 缓存实现类
 */
@Component
@Conditional(DefaultCacheCondition.class)
public class MapCacheHelper {

    @Autowired
    private CacheConfig cacheConfig;
    /**
     * Map 缓存
     * @return
     */
    @Bean
    @Conditional(DefaultCacheCondition.class)
    public Cache mapCacheHelper(){
        return new FIFOCache(cacheConfig.getCapacity(), cacheConfig.getNorepeatTimeout());
    }
}
