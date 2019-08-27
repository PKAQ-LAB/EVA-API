package io.nerv.security.entrypoint;

import cn.hutool.core.util.StrUtil;
import io.nerv.security.service.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 自定义登录验证逻辑
 */
@Component
public class LoginAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token
                = (UsernamePasswordAuthenticationToken) authentication;

        var username = token.getName();
        String password = (String)token.getCredentials();

        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)){
            throw new BadCredentialsException("登录失败,请输入正确的用户名或密码.");
        }

        username = username.trim();
        password = password.trim();

        UserDetails user = userDetailsService.loadUserByUsername(username);
        if (null == user){
            throw new BadCredentialsException("登录失败,该用户不存在.");
        }

        boolean matches = new BCryptPasswordEncoder().matches(password, user.getPassword());

        if (!matches) {
            throw new BadCredentialsException("登录失败,用户名或密码错误.");
        }
        return new UsernamePasswordAuthenticationToken(username, user.getPassword(), user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
