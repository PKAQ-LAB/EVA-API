package org.pkaq.core.auth.config;

import lombok.AllArgsConstructor;
import org.pkaq.core.auth.security.provider.LoginAuthenticationProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author PKAQ
 */
@Configuration
@AllArgsConstructor
public class AuthenticationManagerConfig extends GlobalAuthenticationConfigurerAdapter {
    private final LoginAuthenticationProvider loginAuthenticationProvider;
    private final UserDetailsService userDetailsService;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void init(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(loginAuthenticationProvider)
                // 设置UserDetailsService
                .userDetailsService(userDetailsService)
                // 使用统一密码编码器
                .passwordEncoder(passwordEncoder);
    }

}
