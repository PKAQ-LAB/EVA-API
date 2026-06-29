package org.pkaq.core.auth.security.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.AuthCodes;
import org.pkaq.core.auth.security.domain.JwtUserFactory;
import org.pkaq.core.auth.user.entity.AuthUserEntity;
import org.pkaq.core.auth.user.service.AuthUserService;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 认证用户明细服务
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

    private final AuthUserService authUserService;
    private final EvaConfig evaConfig;

    /**
     * 根据账号加载用户
     *
     * @param account 账号/手机号/邮箱
     * @return UserDetails
     * @throws UsernameNotFoundException 用户不存在
     */
    @Override
    public UserDetails loadUserByUsername(String account) {
        var user = authUserService.getUserWithRoles(account);
        AuthCodes.ACCOUNT_OR_PWD_ERROR.assertNotNull(user);
        ensureTenantAvailable(user);
        return JwtUserFactory.create(user);
    }

    private void ensureTenantAvailable(AuthUserEntity user) {
        if (!evaConfig.isSaasMode()) {
            return;
        }
        if (user.getTenantFrozen() == FrozenEnumm.FROZEN) {
            AuthCodes.LOGIN_TENANT_AUTH_EXPIRED.newException();
        }
        if (user.getTenantExpirationDate() != null && user.getTenantExpirationDate().before(new java.util.Date())) {
            AuthCodes.LOGIN_TENANT_AUTH_EXPIRED.newException();
        }
    }
}
