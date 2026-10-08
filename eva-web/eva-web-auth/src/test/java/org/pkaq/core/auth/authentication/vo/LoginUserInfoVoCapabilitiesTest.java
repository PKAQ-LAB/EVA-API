package org.pkaq.core.auth.authentication.vo;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.authentication.domain.JwtUserDetail;
import org.pkaq.core.auth.spi.ILoginAudit;
import org.pkaq.core.auth.authentication.entrypoint.UrlAuthenticationSuccessHandler;
import org.pkaq.core.auth.spi.ITenantIdentityResolver;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.constant.PlatformCapabilities;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * 登录用户平台能力字段测试。
 *
 * @author PKAQ
 */
class LoginUserInfoVoCapabilitiesTest {
    @Test
    void derivesCapabilityForPlatformAdministratorOnly() {
        EvaConfig config = new EvaConfig();
        config.setMode("platform");
        UrlAuthenticationSuccessHandler handler = new UrlAuthenticationSuccessHandler(
                mock(JwtUtil.class), config, mock(ILoginAudit.class),
                mock(CacheTokenUtil.class), mock(ITenantIdentityResolver.class));
        JwtUserDetail platformAdmin = user(0L);
        JwtUserDetail tenantAdmin = user(101L);

        LoginUserInfoVo platformInfo = ReflectionTestUtils.invokeMethod(
                handler, "buildLoginUserInfo", platformAdmin);
        LoginUserInfoVo tenantInfo = ReflectionTestUtils.invokeMethod(
                handler, "buildLoginUserInfo", tenantAdmin);

        assertEquals(Set.of(PlatformCapabilities.TENANT_INSPECT), platformInfo.getCapabilities());
        assertEquals(Set.of(), tenantInfo.getCapabilities());
    }

    private JwtUserDetail user(long tenantId) {
        return new JwtUserDetail(1L, "admin", tenantId, "pwd", 0L,
                "管理员", "管理员", false,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
