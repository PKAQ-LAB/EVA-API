package io.nerv.security.provider;

import cn.hutool.core.util.StrUtil;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.OathException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 自定义登录验证逻辑
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
     * @param username
     * @param authentication
     * @return
     * @throws OathException
     */
    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws OathException {
        String password = (String)authentication.getCredentials();

        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)){
            throw new OathException(BizCodeEnum.ACCOUNT_OR_PWD_ERROR);
        }

        username = username.trim();

        UserDetails user = this.getUserDetailsService().loadUserByUsername(username);
        if (null == user){
            throw new OathException(BizCodeEnum.ACCOUNT_NOT_EXIST);
        }
        return user;
    }

    /**
     * 校验密码
     * @param userDetails
     * @param authentication
     * @throws OathException
     */
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) throws OathException {
        if (authentication.getCredentials() == null) {
            logger.debug("登录失败，鉴权信息为空");

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    BizCodeEnum.LOGIN_ERROR.getName()));
        }

        String presentedPassword = authentication.getCredentials().toString();


        boolean matches =  this.bCryptPasswordEncoder.matches(presentedPassword, userDetails.getPassword());

        if (!matches) {
            throw new OathException(BizCodeEnum.ACCOUNT_OR_PWD_ERROR);
        }

    }
    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
