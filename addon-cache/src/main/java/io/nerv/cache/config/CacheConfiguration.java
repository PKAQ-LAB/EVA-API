package io.nerv.cache.config;

import cn.hutool.cache.Cache;
import cn.hutool.cache.impl.FIFOCache;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import io.nerv.cache.condition.DefaultCacheCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Slf4j
@Configuration
@EnableConfigurationProperties(CacheConfig.class)
public class CacheConfiguration extends CachingConfigurerSupport {

    @Autowired
    private CacheConfig cacheConfig;

    /*
     * 定义缓存数据 key 生成策略的bean 包名+类名+方法名+所有参数
     */
    public KeyGenerator keyGenerator() {
        return (o, method, objects) -> {
            //格式化缓存key字符串
            StringBuilder sb = new StringBuilder();
            //追加类名
            sb.append(o.getClass().getName());
            //追加方法名
            sb.append(method.getName());
            //遍历参数并且追加
            for (Object obj : objects) {
                sb.append(obj.toString());
            }
            System.out.println("调用Redis缓存Key : " + sb.toString());
            return sb.toString();
        };
    }

    /*
     * 要启用spring缓存支持,需创建一个 CacheManager的 bean，CacheManager 接口有很多实现，这里Redis 的集成，用
     * RedisCacheManager这个实现类 Redis 不是应用的共享内存，它只是一个内存服务器，就像 MySql 似的，
     * 我们需要将应用连接到它并使用某种“语言”进行交互，因此我们还需要一个连接工厂以及一个 Spring 和 Redis 对话要用的
     * RedisTemplate， 这些都是 Redis 缓存所必需的配置，把它们都放在自定义的 CachingConfigurerSupport 中
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
        redisCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.ofMinutes(30L)) //设置缓存的默认超时时间：30分钟
                                                         //如果是空值，不缓存
                                                         .disableCachingNullValues()
                                                         //设置key序列化器
                                                         .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                                                         //设置value序列化器
                                                         .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer((new FastJsonRedisSerializer<>(Object.class))));

        log.debug("自定义RedisCacheManager加载完成");

        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                                .cacheDefaults(redisCacheConfiguration).build();
    }

    /**
     * Map 缓存
     * @return
     */
    @Bean
    @Conditional(DefaultCacheCondition.class)
    public Cache mapCacheHelper(){
        return new FIFOCache(cacheConfig.getCapacity(), cacheConfig.getTimeout());
    }
}