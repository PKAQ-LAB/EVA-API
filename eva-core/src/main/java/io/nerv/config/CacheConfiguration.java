package io.nerv.config;

import cn.hutool.cache.Cache;
import cn.hutool.cache.impl.FIFOCache;
import io.nerv.core.cache.config.CacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存配置类
 */
@Configuration
@EnableConfigurationProperties(CacheConfig.class)
public class CacheConfiguration {
    @Autowired
    private CacheConfig cacheConfig;
    /**
     * Map缓存
     * @return
     */
    @Bean("fifoCache")
    public Cache getMapCache(){
        return new FIFOCache(cacheConfig.getCapacity(), cacheConfig.getTimeout());
    }

}
