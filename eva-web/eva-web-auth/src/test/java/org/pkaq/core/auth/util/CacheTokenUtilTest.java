package org.pkaq.core.auth.util;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.session.RedisSessionStore;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.properties.EvaConfig;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Token Redis 会话存储测试。
 *
 * @author PKAQ
 */
class CacheTokenUtilTest {

    /**
     * Token 必须按租户和用户复合范围保存。
     */
    @Test
    void shouldUseTenantAndUserCompositeScope() {
        RedisSessionStore sessionStore = mock(RedisSessionStore.class);
        EvaConfig evaConfig = new EvaConfig();
        Object value = new Object();
        when(sessionStore.get(7L, 11L)).thenReturn(value);
        CacheTokenUtil util = new CacheTokenUtil(mock(JwtUtil.class), sessionStore, evaConfig);

        util.saveToken(7L, 11L, value);
        assertSame(value, util.getToken(7L, 11L));
        assertNull(util.getToken(8L, 11L));
        util.removeToken(7L, 11L);

        verify(sessionStore).save(7L, 11L, value, Duration.ofMillis(evaConfig.getJwt().getTtl()));
        verify(sessionStore).remove(7L, 11L);
    }

    /**
     * 在线会话查询必须委托给 Redis 按租户筛选。
     */
    @Test
    void shouldListSessionsWithinTenantScope() {
        RedisSessionStore sessionStore = mock(RedisSessionStore.class);
        Map<String, Object> expected = Map.of("7:11", Map.of("device", "web"));
        when(sessionStore.list(7L)).thenReturn(expected);
        CacheTokenUtil util = new CacheTokenUtil(mock(JwtUtil.class), sessionStore, new EvaConfig());

        Map<?, ?> result = util.getTokens(7L);

        assertEquals(expected, result);
        verify(sessionStore).list(7L);
    }
}
