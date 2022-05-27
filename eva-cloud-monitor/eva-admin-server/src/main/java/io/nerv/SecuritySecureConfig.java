package io.nerv;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecuritySecureConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl( "/");

        http.authorizeRequests()
                //授予对所有静态资产和登录页面的公共访问权限
                .antMatchers( "/assets/**").permitAll()
                .antMatchers( "/login").permitAll()
                .antMatchers( "/instances/**").permitAll()
                .antMatchers( "/actuator/**").permitAll()
                //必须对每个其他请求进行身份验证
                .anyRequest().authenticated()
                .and()
                //配置登录和注销
                .formLogin().loginPage( "/login").successHandler(successHandler).and()
                .logout().logoutUrl( "/logout").and()
                //启用HTTP-Basic支持。这是Spring Boot Admin Client注册所必需的
                .httpBasic().and()
                .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringAntMatchers(
                        //	禁用CRSF保护Spring引导管理客户端用来注册的端点。
                         "/instances",
                        // 禁用执行器端点的CRSF保护
                         "/actuator/**");
    }
}