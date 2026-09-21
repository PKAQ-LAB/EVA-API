package org.pkaq.core.auth.security.provider;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 携带服务端已解析租户身份的登录认证令牌。
 *
 * @author PKAQ
 */
public class TenantLoginAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final Long tenantId;

    public TenantLoginAuthenticationToken(Object principal, Object credentials, Long tenantId,
                                          Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        this.tenantId = tenantId;
    }

    public Long getTenantId() {
        return tenantId;
    }
}
