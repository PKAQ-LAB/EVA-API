package io.nerv.core.cache.config

import com.github.benmanes.caffeine.cache.Caffeine
import io.nerv.core.cache.condition.DefaultCacheCondition
import io.nerv.properties.Cache
import io.nerv.properties.EvaConfig
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

@Configuration
@Conditional(DefaultCacheCondition::class)
class CaffeineConfiguration {
    val log = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private val evaConfig: EvaConfig? = null

    /**
     * * 配置缓存管理器
     * *
     * * @return 缓存管理器
     */
    @Bean
    fun cacheManager(): CacheManager {
        log.debug("初始化 Caffeine 緩存 --- --- --- -->")
        val defaultCache = CaffeineCache("default", Caffeine.newBuilder()
                .expireAfterAccess(3, TimeUnit.HOURS) // 初始的缓存空间大小
                .initialCapacity(100) // 缓存的最大条数
                .maximumSize(1000)
                .build())
        val caches = evaConfig!!.cache!!.config!!.stream().map { item: Cache.CacheObject -> buildCache(item.name, item.spec) }.collect(Collectors.toList())
        caches.add(defaultCache)
        val simpleCacheManager = SimpleCacheManager()
        simpleCacheManager.setCaches(caches)
        return simpleCacheManager
    }

    /**
     * 构建CaffeineCache对象
     * @param name
     * @param initialCapacity
     * @param secondsToExpire
     * @return
     */
    private fun buildCache(name: String, initialCapacity: Int, secondsToExpire: Int): CaffeineCache {
        return CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterAccess(secondsToExpire.toLong(), TimeUnit.MILLISECONDS) // 初始的缓存空间大小
                .initialCapacity(initialCapacity) // 缓存的最大条数
                .maximumSize(1000)
                .build())
    }

    /**
     * 构建CaffeineCache对象
     * @param name
     * @return
     */
    private fun buildCache(name: String, spec: String?): CaffeineCache {
        return CaffeineCache(name, Caffeine.from(spec).build())
    }
}