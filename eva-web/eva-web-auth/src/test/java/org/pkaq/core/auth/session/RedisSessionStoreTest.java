package org.pkaq.core.auth.session;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis 在线会话状态存储测试。
 *
 * @author PKAQ
 */
class RedisSessionStoreTest {

    /**
     * 会话键必须包含可信租户和用户范围。
     */
    @Test
    void shouldStoreSessionWithTenantNamespace() {
        RedisTemplate<Object, Object> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<Object, Object> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Map<String, Object> value = Map.of("token", "alpha");
        Duration ttl = Duration.ofHours(1);
        when(valueOperations.get("eva:session:7:11:web-1")).thenReturn(value);
        RedisSessionStore store = new RedisSessionStore(redisTemplate);

        store.save(7L, 11L, "web-1", value, ttl);
        assertSame(value, store.get(7L, 11L, "web-1"));
        store.remove(7L, 11L, "web-1");

        verify(valueOperations).set("eva:session:7:11:web-1", value, ttl);
        verify(redisTemplate).delete("eva:session:7:11:web-1");
    }
}
