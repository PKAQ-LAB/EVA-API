package org.pkaq.test;

import org.pkaq.core.cache.util.RedisUtil;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

/**
 * 集成测试缓存替身，避免测试依赖外部 Redis 进程。
 *
 * @author PKAQ
 */
@Profile("test")
@Configuration(proxyBeanMethods = false)
public class TestCacheConfiguration {

    /**
     * 提供仅用于测试的内存 CacheManager。
     *
     * @return 测试缓存管理器
     */
    @Bean
    @Primary
    public CacheManager testCacheManager() {
        return new ConcurrentMapCacheManager("uploadfiles", "dictdata");
    }

    /**
     * 提供仅用于不访问安全缓存的上下文测试替身。
     *
     * @return Redis 操作工具替身
     */
    @Bean
    @Primary
    public RedisUtil testRedisUtil() {
        return mock(RedisUtil.class);
    }
}
