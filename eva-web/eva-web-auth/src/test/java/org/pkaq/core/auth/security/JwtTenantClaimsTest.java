package org.pkaq.core.auth.security;

import org.junit.jupiter.api.Test;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.Jwt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JWT租户声明兼容性测试。
 *
 * @author PKAQ
 */
class JwtTenantClaimsTest {

    @Test
    void shouldPreserveTenantClaimsWhenRefreshingAccessToken() {
        JwtUtil jwtUtil = new JwtUtil(config());
        String token = jwtUtil.build(60_000L, 11L, "admin", List.of(3L), 5L, 7L, 9L);

        String refreshed = jwtUtil.refreshToken(token);

        assertTrue(jwtUtil.valid(refreshed));
        assertEquals(7L, jwtUtil.getTenantId(refreshed));
        assertEquals(9L, jwtUtil.getSchemaGeneration(refreshed));
        assertEquals(11L, jwtUtil.getUid(refreshed));
    }

    @Test
    void shouldKeepLegacyBuilderAsPlatformIdentity() {
        JwtUtil jwtUtil = new JwtUtil(config());
        String token = jwtUtil.build(60_000L, 11L, "admin", List.of(), 0L);

        assertEquals(0L, jwtUtil.getTenantId(token));
        assertEquals(0L, jwtUtil.getSchemaGeneration(token));
    }

    private EvaConfig config() {
        EvaConfig config = new EvaConfig();
        Jwt jwt = new Jwt();
        jwt.setSecert("01234567890123456789012345678901");
        jwt.setSign("eva-test");
        jwt.setAlphaTtl(60_000L);
        config.setJwt(jwt);
        return config;
    }
}
