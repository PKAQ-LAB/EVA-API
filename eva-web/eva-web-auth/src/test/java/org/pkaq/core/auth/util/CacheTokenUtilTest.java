package org.pkaq.core.auth.util;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;
import org.pkaq.core.cache.util.RedisUtil;
import org.pkaq.core.jwt.JwtUtil;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Token缓存租户隔离测试。
 *
 * @author PKAQ
 */
class CacheTokenUtilTest {

    @Test
    void shouldUseTenantAndUserCompositeKey() {
        Cache cache = mock(Cache.class);
        CacheManager manager = mock(CacheManager.class);
        when(manager.getCache("token")).thenReturn(cache);
        Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);
        Object value = new Object();
        when(cache.get("7:11")).thenReturn(wrapper);
        when(wrapper.get()).thenReturn(value);
        CacheTokenUtil util = new CacheTokenUtil(mock(JwtUtil.class), mock(RedisUtil.class), manager);

        util.saveToken(7L, 11L, value);
        assertSame(value, util.getToken(7L, 11L));
        assertNull(util.getToken(8L, 11L));
        util.removeToken(7L, 11L);

        verify(cache).put("7:11", value);
        verify(cache).evict("7:11");
    }

    @Test
    void shouldFilterCaffeineEntriesBeforeReturningValues() {
        CaffeineCache cache = new CaffeineCache("token", Caffeine.newBuilder().build());
        CacheManager manager = mock(CacheManager.class);
        when(manager.getCache("token")).thenReturn(cache);
        cache.put("7:11", Map.of("device", "web"));
        cache.put("70:12", Map.of("device", "other"));
        CacheTokenUtil util = new CacheTokenUtil(mock(JwtUtil.class), mock(RedisUtil.class), manager);

        Map<?, ?> result = util.getTokens(7L);

        assertEquals(1, result.size());
        assertEquals(Map.of("device", "web"), result.get("7:11"));
    }
}
