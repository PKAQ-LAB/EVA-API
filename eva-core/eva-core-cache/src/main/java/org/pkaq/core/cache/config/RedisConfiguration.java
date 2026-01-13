package org.pkaq.core.cache.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.cache.condition.RedisCacheCondition;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author PKAQ
 */
@Slf4j
@Configuration
@Conditional(RedisCacheCondition.class)
@RequiredArgsConstructor
public class RedisConfiguration {

    private final EvaConfig evaConfig;

    /**
     * @return 返回类型
     * @Description: 防止redis入库序列化乱码的问题
     */
    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.debug("初始化 Redis 緩存 --- --- --- -->");
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //key序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //value序列化
        redisTemplate.setValueSerializer(new JacksonJsonRedisSerializer<>(Object.class));

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveRedisTemplate.class)
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        // String序列化器 (用于Key)
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 配置序列化上下文
        RedisSerializationContext<String, Object> serializationContext =
                RedisSerializationContext.<String, Object>newSerializationContext()
                        .key(stringSerializer)
                        .value(new JacksonJsonRedisSerializer<>(Object.class))
                        .hashKey(stringSerializer)
                        .hashValue(new JacksonJsonRedisSerializer<>(Object.class))
                        .build();

        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        log.debug("初始化 redis 緩存 --- --- --- -->");
        RedisCacheConfiguration defaultCache = buildCache(60 * 30L);

        if (null != evaConfig.getCache() && null != evaConfig.getCache().getConfig()) {
            Map<String, RedisCacheConfiguration> cacheMap = HashMap.newHashMap(evaConfig.getCache().getConfig().size());

            evaConfig.getCache().getConfig().forEach(item -> cacheMap.put(item.getName(), buildCache(item.getSecondsToExpire())));

            return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                    .cacheDefaults(defaultCache)
                    .initialCacheNames(cacheMap.keySet())
                    .withInitialCacheConfigurations(cacheMap)
                    .build();
        } else {
            return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                    .cacheDefaults(defaultCache)
                    .build();
        }

    }

    /**
     * 要启用spring缓存支持,需创建一个 CacheManager的 bean，CacheManager 接口有很多实现，这里Redis 的集成，用
     * RedisCacheManager这个实现类 Redis 不是应用的共享内存，它只是一个内存服务器，就像 MySql 似的，
     * 我们需要将应用连接到它并使用某种“语言”进行交互，因此我们还需要一个连接工厂以及一个 Spring 和 Redis 对话要用的
     * RedisTemplate， 这些都是 Redis 缓存所必需的配置，把它们都放在自定义的 CachingConfigurerSupport 中
     */
    private RedisCacheConfiguration buildCache(long secondsToExpire) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
        //设置缓存的默认超时时间：30分钟
        redisCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.ofMillis(secondsToExpire))
                //如果是空值，不缓存
                .disableCachingNullValues()
                //设置key序列化器
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                //设置value序列化器
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer((new JacksonJsonRedisSerializer<>(Object.class))));

        log.debug("自定义RedisCacheManager加载完成");

        return redisCacheConfiguration;
    }

}