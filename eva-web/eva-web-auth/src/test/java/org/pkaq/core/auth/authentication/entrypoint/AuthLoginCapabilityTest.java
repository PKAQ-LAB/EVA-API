package org.pkaq.core.auth.authentication.entrypoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.authentication.domain.JwtUserDetail;
import org.pkaq.core.auth.spi.ILoginAudit;
import org.pkaq.core.auth.spi.ITenantIdentityResolver;
import org.pkaq.core.auth.tenant.TenantLoginIdentity;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.mvc.vo.AuthBootstrapResponse;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 使用真实登录成功处理器与ResponseUtil验证外层能力及原登录数据结构。
 *
 * @author PKAQ
 * @date 2026-10-28
 */
class AuthLoginCapabilityTest {
    @Test
    void serializesCurrentEnable() throws Exception {
        EvaConfig config = new EvaConfig();
        config.getJwt().setPersistence(false);
        JwtUtil jwt = mock(JwtUtil.class);
        ITenantIdentityResolver resolver = mock(ITenantIdentityResolver.class);
        when(resolver.resolveId(0L)).thenReturn(new TenantLoginIdentity(0L, 0L));
        JwtUserDetail user = new JwtUserDetail(9L, "synthetic-login", 0L, "synthetic-password", 0L,
                "合成用户", "合成别名", false, List.of());
        user.setRoleIds(List.of());
        when(jwt.newSessionId()).thenReturn("synthetic-session");
        when(jwt.build(config.getJwt().getAlphaTtl(), 9L, user.getAccount(), List.of(), 0L,
                0L, 0L, "synthetic-session")).thenReturn("synthetic-access");
        when(jwt.buildRefreshToken(config.getJwt().getBravoTtl(), 9L, user.getAccount(), List.of(), 0L,
                0L, 0L, "synthetic-session")).thenReturn("synthetic-refresh");
        UrlAuthenticationSuccessHandler handler = new UrlAuthenticationSuccessHandler(jwt, config,
                mock(ILoginAudit.class), mock(CacheTokenUtil.class), resolver);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        for (boolean enabled : new boolean[] {true, false}) {
            config.getResourcePermission().setEnable(enabled);
            config.getResourcePermission().setStrict(!enabled);
            MockHttpServletResponse response = new MockHttpServletResponse();
            handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication);
            JsonNode json = new ObjectMapper().readTree(response.getContentAsString());
            assertTrue(json.path("success").booleanValue());
            assertTrue(json.path("resourcePermissionEnabled").isBoolean());
            assertEquals(enabled, json.path("resourcePermissionEnabled").booleanValue());
            assertEquals("0000", json.path("code").textValue());
            assertTrue(json.path("data").isObject());
            assertEquals("synthetic-access", json.path("data").path("access_token").textValue());
            assertEquals("synthetic-refresh", json.path("data").path("refresh_token").textValue());
            assertEquals("synthetic-login", json.path("data").path("user_info").path("account").textValue());
            assertFalse(json.path("data").has("resourcePermissionEnabled"));
            assertFalse(json.has("mtype"));
            assertFalse(json.has("args"));
        }
    }

    @Test
    void rejectsFailedCapability() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new UrlAuthenticationFailureHandler(mock(ILoginAudit.class)).onAuthenticationFailure(
                new MockHttpServletRequest(), response, new BadCredentialsException("合成登录失败"));
        JsonNode json = new ObjectMapper().readTree(response.getContentAsString());
        assertFalse(json.path("success").booleanValue());
        assertFalse(json.has("resourcePermissionEnabled"));
        assertThrows(IllegalArgumentException.class,
                () -> AuthBootstrapResponse.fromSuccess(Response.failure("合成失败"), false));
        assertThrows(IllegalArgumentException.class, () -> AuthBootstrapResponse.fromSuccess(null, false));
    }

    @Test
    void preservesMetadataAndFalse() throws Exception {
        Object[] args = {"合成欢迎参数"};
        Response<List<String>> original = Response.success(List.of("合成数据"));
        original.setCode("synthetic-success");
        original.setMessage("合成消息{0}");
        original.setMtype("synthetic-message-type");
        original.setArgs(args);
        AuthBootstrapResponse<List<String>> snapshot = AuthBootstrapResponse.fromSuccess(original, false);
        assertEquals(original.getCode(), snapshot.getCode());
        assertEquals(original.getMessage(), snapshot.getMessage());
        assertSame(original.getMtype(), snapshot.getMtype());
        assertSame(args, snapshot.getArgs());
        assertSame(original.getData(), snapshot.getData());
        ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        JsonNode json = mapper.readTree(mapper.writeValueAsString(snapshot));
        assertTrue(json.path("resourcePermissionEnabled").isBoolean());
        assertFalse(json.path("resourcePermissionEnabled").booleanValue());
        assertFalse(json.has("mtype"));
        assertFalse(json.has("args"));
    }
}
