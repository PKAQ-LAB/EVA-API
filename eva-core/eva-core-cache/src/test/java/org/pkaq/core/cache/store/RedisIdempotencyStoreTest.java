package org.pkaq.core.cache.store;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis 幂等状态存储测试。
 *
 * @author PKAQ
 */
class RedisIdempotencyStoreTest {

    /**
     * 幂等键必须通过 Redis 原子写入获取。
     */
    @Test
    void shouldAcquireKeyAtomicallyWithTtl() {
        RedisTemplate<Object, Object> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<Object, Object> valueOperations = mock(ValueOperations.class);
        Duration ttl = Duration.ofSeconds(2);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent("eva:idempotency:request", Boolean.TRUE, ttl))
                .thenReturn(true, false);
        RedisIdempotencyStore store = new RedisIdempotencyStore(redisTemplate);

        assertTrue(store.acquire("request", ttl));
        assertFalse(store.acquire("request", ttl));

        verify(valueOperations, org.mockito.Mockito.times(2))
                .setIfAbsent("eva:idempotency:request", Boolean.TRUE, ttl);
    }
}
