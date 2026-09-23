package org.pkaq.core.auth.util;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.session.RedisSessionStore;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.pkaq.web.core.client.ClientInfoResolver;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        when(sessionStore.get(7L, 11L, "web-1")).thenReturn(value);
        CacheTokenUtil util = new CacheTokenUtil(mock(JwtUtil.class), sessionStore,
                new ClientInfoResolver(evaConfig), evaConfig);

        util.saveToken(7L, 11L, "web-1", value);
        assertSame(value, util.getToken(7L, 11L, "web-1"));
        assertNull(util.getToken(8L, 11L, "web-1"));
        util.removeToken(7L, 11L);

        verify(sessionStore).save(7L, 11L, "web-1", value,
                Duration.ofMillis(evaConfig.getJwt().getBravoTtl()));
        verify(sessionStore).removeUser(7L, 11L);
    }

    /**
     * 在线会话查询必须委托给 Redis 按租户筛选。
     */
    @Test
    void shouldListSessionsWithinTenantScope() {
        RedisSessionStore sessionStore = mock(RedisSessionStore.class);
        Map<String, Object> expected = Map.of("7:11:web-1", Map.of("device", "web"));
        when(sessionStore.list(7L)).thenReturn(expected);
        EvaConfig evaConfig = new EvaConfig();
        CacheTokenUtil util = new CacheTokenUtil(mock(JwtUtil.class), sessionStore,
                new ClientInfoResolver(evaConfig), evaConfig);

        Map<?, ?> result = util.getTokens(7L);

        assertEquals(expected, result);
        verify(sessionStore).list(7L);
    }

    /**
     * Redis 仅保存Token摘要，并分别校验access token与refresh token。
     */
    @Test
    void shouldStoreHashesAndMatchTokensWithinExactSession() {
        RedisSessionStore sessionStore = mock(RedisSessionStore.class);
        EvaConfig evaConfig = new EvaConfig();
        evaConfig.getJwt().setSecert("01234567890123456789012345678901");
        JwtUtil jwtUtil = new JwtUtil(evaConfig);
        CacheTokenUtil util = new CacheTokenUtil(jwtUtil, sessionStore,
                new ClientInfoResolver(evaConfig), evaConfig);
        String accessToken = jwtUtil.build(60_000L, 11L, "admin", java.util.List.of(), 0L,
                7L, 1L, "web-1");
        String refreshToken = jwtUtil.buildRefreshToken(60_000L, 11L, "admin", java.util.List.of(), 0L,
                7L, 1L, "web-1");

        Map<String, Object> value = util.buildCacheValue(
                new MockHttpServletRequest(), 11L, accessToken, refreshToken);
        when(sessionStore.get(7L, 11L, "web-1")).thenReturn(value);

        assertFalse(value.containsValue(accessToken));
        assertFalse(value.containsValue(refreshToken));
        assertTrue(util.matchesAccessToken(7L, 11L, "web-1", accessToken));
        assertTrue(util.matchesRefreshToken(7L, 11L, "web-1", refreshToken));
        assertFalse(util.matchesRefreshToken(7L, 11L, "web-1", accessToken));
    }
}
