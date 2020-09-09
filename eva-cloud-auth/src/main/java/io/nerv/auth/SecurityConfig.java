package io.nerv.auth;

import cn.hutool.core.collection.CollUtil;
import io.nerv.properties.EvaConfig;
import io.nerv.user.AuthUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 该配置类，主要处理用户名和密码的校验等事宜
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private EvaConfig evaConfig;

    @Autowired
    private AuthUserService userService;
    /**
     * 注册一个认证管理器对象到容器
     *
     * @return
     * @throws Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

/**
 * 添加一个内置的超级管理员用户
 */
//    @Override
//    public void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.authenticationProvider(loginAuthenticationProvider)
//                // 设置UserDetailsService
//                .userDetailsService(this.userService)
//                // 使用BCrypt进行密码的hash
//                .passwordEncoder(passwordEncoder());
//        // 默认超级用户
//        auth.inMemoryAuthentication()
//                .withUser("toor")
//                .password(new BCryptPasswordEncoder().encode("nerv_toor_eva"))
//                .roles("ADMIN");
//    }

    /**
     * 密码编码对象(密码不进行加密处理)
     *
     * @return
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 处理用户名和密码验证事宜
     * 1)客户端传递username和password参数到认证服务器
     * 2)一般来说，username和password会存储在数据库中的用户表中
     * 3)根据用户表中数据，验证当前传递过来的用户信息的合法性
     */
    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors()
                .and()
                // 关闭csrf 由于使用的是JWT，我们这里不需要csrf
                .csrf().disable()
                //允许加载iframe内容 X-Frame-Options
                .headers().frameOptions().disable()
                .xssProtection().block(true)
                .and()
                // 适配IE
                .addHeaderWriter(new StaticHeadersWriter("P3P","CP='CAO IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT'"))
                .and()
                // 基于token，所以不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 对于获取token的rest api要允许匿名访问
                .antMatchers(evaConfig
                                .getSecurity()
                                .getAnonymous()).permitAll()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                // allow anonymous resource requests
            .and()
            .ignoring()
            .antMatchers(
                HttpMethod.GET,
                "/",
                "/static/**",
                "/*.html",
                "/*.xls",
                "/*.xlsx",
                "/*.doc",
                "/*.docx",
                "/*.pdf",
                "/favicon.ico",
                "/**/*.html",
                "/**/*.css",
                "/**/*.js",
                "/webjars/**",
                "/swagger-resources/**",
                "/*/api-docs"
            );

    }
}