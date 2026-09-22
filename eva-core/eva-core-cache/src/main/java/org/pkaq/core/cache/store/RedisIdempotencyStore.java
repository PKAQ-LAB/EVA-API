package org.pkaq.core.cache.store;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis 幂等状态存储。
 *
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class RedisIdempotencyStore {

    private static final String KEY_PREFIX = "eva:idempotency:";

    private final RedisTemplate<Object, Object> redisTemplate;

    /**
     * 在指定时间内首次占用幂等键。
     *
     * @param key 业务幂等键
     * @param ttl 锁定时间
     * @return 是否成功占用
     */
    public boolean acquire(String key, Duration ttl) {
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + key, Boolean.TRUE, ttl);
        return Boolean.TRUE.equals(acquired);
    }
}
