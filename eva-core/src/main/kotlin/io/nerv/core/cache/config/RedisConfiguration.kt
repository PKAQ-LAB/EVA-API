package io.nerv.core.cache.config

import io.nerv.core.cache.condition.RedisCacheCondition
import io.nerv.core.util.JsonUtil
import io.nerv.properties.Cache
import io.nerv.properties.EvaConfig
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.cache.RedisCacheWriter
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration
import java.util.*

@Slf4j
@Configuration
@Conditional(RedisCacheCondition::class)
class RedisConfiguration {
    @Autowired
    private val jsonUtil: JsonUtil? = null

    @Autowired
    private val evaConfig: EvaConfig? = null

    /**
     * @Description: 防止redis入库序列化乱码的问题
     * @return     返回类型
     * @date 2018/4/12 10:54
     */
    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory?): RedisTemplate<Any, Any> {
        RedisConfiguration.log.debug("初始化 Redis 緩存 --- --- --- -->")
        val redisTemplate = RedisTemplate<Any, Any>()
        redisTemplate.connectionFactory = redisConnectionFactory
        //key序列化
        redisTemplate.keySerializer = StringRedisSerializer()
        //value序列化
        redisTemplate.valueSerializer = Jackson2JsonRedisSerializer(Any::class.java)
        redisTemplate.afterPropertiesSet()
        return redisTemplate
    }

    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory?): CacheManager {
        RedisConfiguration.log.debug("初始化 redis 緩存 --- --- --- -->")
        val defaultCache = buildCache(60 * 30L)
        val cacheMap: MutableMap<String?, RedisCacheConfiguration?> = HashMap<Any?, Any?>(evaConfig!!.cache!!.config!!.size)
        evaConfig.cache!!.config!!.stream().forEach { item: Cache.CacheObject -> cacheMap[item.name] = buildCache(item.secondsToExpire.toLong()) }
        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                .cacheDefaults(defaultCache)
                .initialCacheNames(cacheMap.keys)
                .withInitialCacheConfigurations(cacheMap)
                .build()
    }

    /*
     * 要启用spring缓存支持,需创建一个 CacheManager的 bean，CacheManager 接口有很多实现，这里Redis 的集成，用
     * RedisCacheManager这个实现类 Redis 不是应用的共享内存，它只是一个内存服务器，就像 MySql 似的，
     * 我们需要将应用连接到它并使用某种“语言”进行交互，因此我们还需要一个连接工厂以及一个 Spring 和 Redis 对话要用的
     * RedisTemplate， 这些都是 Redis 缓存所必需的配置，把它们都放在自定义的 CachingConfigurerSupport 中
     */
    private fun buildCache(secondsToExpire: Long): RedisCacheConfiguration {
        var redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
        //设置缓存的默认超时时间：30分钟
        redisCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.ofMillis(secondsToExpire)) //如果是空值，不缓存
                .disableCachingNullValues() //设置key序列化器
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())) //设置value序列化器
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(Jackson2JsonRedisSerializer(Any::class.java)))
        RedisConfiguration.log.debug("自定义RedisCacheManager加载完成")
        return redisCacheConfiguration
    }
}