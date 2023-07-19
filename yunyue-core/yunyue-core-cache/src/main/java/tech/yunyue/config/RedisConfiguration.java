package tech.yunyue.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.serializer.*;
import tech.yunyue.core.cache.condition.RedisCacheCondition;
import tech.yunyue.core.properties.EvaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author PKAQ
 */
@Slf4j
@Configuration
@Conditional(RedisCacheCondition.class)
@RequiredArgsConstructor
@EnableCaching
public class RedisConfiguration {

    private final EvaConfig evaConfig;

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm:ss";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);

    /**
     * @return 返回类型
     * @Description: 防止redis入库序列化乱码的问题
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.debug("初始化 Redis 緩存 --- --- --- -->");
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //key序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //value序列化
        redisTemplate.setValueSerializer(getValueSerializer());

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    private RedisSerializer getValueSerializer(){
        // 指定相应的序列化方案
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();
        // 通过反射获取Mapper对象, 增加一些配置, 增强兼容性
        try {
            Field field = GenericJackson2JsonRedisSerializer.class.getDeclaredField("mapper");
            field.setAccessible(true);
            ObjectMapper objectMapper = (ObjectMapper) field.get(valueSerializer);
            // 配置[忽略未知字段]
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            // 配置[时间类型转换]
            JavaTimeModule timeModule = new JavaTimeModule();
            // LocalDateTime序列化与反序列化
            timeModule.addSerializer(new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
            timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
            // LocalDate序列化与反序列化
            timeModule.addSerializer(new LocalDateSerializer(DATE_FORMATTER));
            timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));
            // LocalTime序列化与反序列化
            timeModule.addSerializer(new LocalTimeSerializer(TIME_FORMATTER));
            timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(TIME_FORMATTER));
            objectMapper.registerModule(timeModule);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return valueSerializer;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        log.debug("初始化 redis 緩存 --- --- --- -->");
        RedisCacheConfiguration defaultCache = buildCache(60 * 30L);

        if (null != evaConfig.getCache() && null != evaConfig.getCache().getConfig()) {
            Map<String, RedisCacheConfiguration> cacheMap = new HashMap(evaConfig.getCache().getConfig().size());

            evaConfig.getCache().getConfig().stream().forEach(item -> {
                cacheMap.put(item.getName(), buildCache(item.getSecondsToExpire()));
            });

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
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer((new Jackson2JsonRedisSerializer(Object.class))));

        log.debug("自定义RedisCacheManager加载完成");

        return redisCacheConfiguration;
    }

}
