package org.pkaq.core.auth.security.provider;

import lombok.Getter;
import lombok.Setter;
import org.pkaq.core.auth.AuthCodes;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 自定义登录认证逻辑
 *
 * @author PKAQ
 */
@Getter
@Setter
@Component
public class LoginAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private UserDetailsService userDetailsService;

    private PasswordEncoder passwordEncoder;

    public LoginAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.setHideUserNotFoundExceptions(true);
    }

    /**
     * 查询用户
     *
     * @param username       账号
     * @param authentication 认证信息
     * @return 用户明细
     */
    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) {
        String password = (String) authentication.getCredentials();

        AuthCodes.ACCOUNT_OR_PWD_ERROR.assertNotNull(username);
        AuthCodes.ACCOUNT_OR_PWD_ERROR.assertNotNull(password);

        username = username.trim();

        UserDetails user = this.getUserDetailsService().loadUserByUsername(username);

        AuthCodes.ACCOUNT_OR_PWD_ERROR.assertNotNull(user);

        return user;
    }

    /**
     * 校验密码
     *
     * @param userDetails    用户明细
     * @param authentication 认证信息
     */
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) {
        AuthCodes.LOGIN_ERROR.assertNotNull(authentication.getCredentials(), AuthCodes.ACCOUNT_OR_PWD_ERROR);

        String presentedPassword = String.valueOf(authentication.getCredentials());

        boolean matches = this.passwordEncoder.matches(presentedPassword, userDetails.getPassword());

        if (!matches) {
            throw new BadCredentialsException(AuthCodes.ACCOUNT_OR_PWD_ERROR.getMsg());
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}

