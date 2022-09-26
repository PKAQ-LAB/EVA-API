package io.nerv.core.cache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.nerv.core.cache.condition.DefaultCacheCondition;
import io.nerv.properties.EvaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Configuration
@Conditional(DefaultCacheCondition.class)
@RequiredArgsConstructor
public class CaffeineConfiguration {

    private final EvaConfig evaConfig;
    /**
     *     * 配置缓存管理器
     *     *
     *     * @return 缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        log.debug("初始化 Caffeine 緩存 --- --- --- -->");

        CaffeineCache defaultCache = new CaffeineCache("default", Caffeine.newBuilder()
                .expireAfterAccess(3, TimeUnit.HOURS)
                // 初始的缓存空间大小
                .initialCapacity(100)
                // 缓存的最大条数
                .maximumSize(1000)
                .build());

        List<CaffeineCache> caches = evaConfig.getCache().getConfig().stream().map(item ->
            buildCache(item.getName(), item.getSpec())
        ).collect(Collectors.toList());

        caches.add(defaultCache);

        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(caches);

        return simpleCacheManager;
    }

    /**
     * 构建CaffeineCache对象
     * @param name
     * @param initialCapacity
     * @param secondsToExpire
     * @return
     */
    private CaffeineCache buildCache(String name, int initialCapacity, int secondsToExpire){
       return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterAccess(secondsToExpire, TimeUnit.MILLISECONDS)
                // 初始的缓存空间大小
                .initialCapacity(initialCapacity)
                // 缓存的最大条数
                .maximumSize(1000)
                .build());
    }

    /**
     * 构建CaffeineCache对象
     * @param name
     * @return
     */
    private CaffeineCache buildCache(String name, String spec){
        return new CaffeineCache(name, Caffeine.from(spec).build());
    }
}
