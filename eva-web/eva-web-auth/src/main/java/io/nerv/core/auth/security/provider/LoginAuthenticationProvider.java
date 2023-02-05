package io.nerv.core.auth.security.provider;

import io.nerv.core.enums.BizCodeEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 自定义登录验证逻辑
 * @author
 */
@Getter
@Setter
@Component
public class LoginAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private UserDetailsService userDetailsService;

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private PasswordEncoder passwordEncoder;

    public LoginAuthenticationProvider(UserDetailsService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.passwordEncoder = passwordEncoder;
        this.setHideUserNotFoundExceptions(false);
    }

    /**
     * 查询用户
     *
     * @param username
     * @param authentication
     * @return
     */
    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) {
        String password = (String) authentication.getCredentials();

        BizCodeEnum.ACCOUNT_OR_PWD_ERROR.assertNotNull(username);
        BizCodeEnum.ACCOUNT_OR_PWD_ERROR.assertNotNull(password);

        username = username.trim();

        UserDetails user = this.getUserDetailsService().loadUserByUsername(username);

        BizCodeEnum.ACCOUNT_NOT_EXIST.assertNotNull(user);

        return user;
    }

    /**
     * 校验密码
     *
     * @param userDetails
     * @param authentication
     */
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) {
        BizCodeEnum.LOGIN_ERROR.assertNotNull(authentication.getCredentials(), BizCodeEnum.ACCOUNT_OR_PWD_ERROR);

        String presentedPassword = authentication.getCredentials().toString();

        boolean matches = this.bCryptPasswordEncoder.matches(presentedPassword, userDetails.getPassword());

        if (!matches) {
            BizCodeEnum.ACCOUNT_OR_PWD_ERROR.newException(AuthenticationException.class);
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
