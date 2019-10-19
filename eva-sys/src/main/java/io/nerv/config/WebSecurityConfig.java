package io.nerv.config;

import cn.hutool.core.collection.CollUtil;
import io.nerv.properties.EvaConfig;
import io.nerv.security.entrypoint.*;
import io.nerv.security.filter.JwtAuthFilter;
import io.nerv.security.provider.JwtUsernamePasswordAuthenticationFilter;
import io.nerv.security.provider.LoginAuthenticationProvider;
import io.nerv.security.provider.UrlFilterSecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * @author PKAQ
 * 1.必须保证prePostEnabled开启 否则@PreAuthorize("hasRole('ROLE_ADMIN')")无效
 * 2.启用EnableConfigurationProperties以使ConfigurationProperties生效
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${eva.security.anonymous}")
    private String[] anonymous;

    @Autowired
    private EvaConfig evaConfig;

    @Autowired
    private LoginAuthenticationProvider loginAuthenticationProvider;

    @Autowired
    private UrlAuthenticationSuccessHandler urlAuthenticationSuccessHandler;

    @Autowired
    private UrlAuthenticationFailureHandler urlAuthenticationFailureHandler;

    @Autowired
    private UrlLogoutSuccessHandler urlLogoutSuccessHandler;

    @Autowired
    private UrlAccessDeniedHandler urlAccessDeniedHandler;

    @Autowired
    private UnauthorizedHandler unauthorizedHandler;

    @Autowired
    private UrlFilterSecurityInterceptor urlFilterSecurityInterceptor;

    /** Spring会自动寻找同样类型的具体类注入，这里就是JwtUserDetailsServiceImpl了**/
    @Autowired
    @Qualifier("jwtUserDetailsService")
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(loginAuthenticationProvider)
            // 设置UserDetailsService
            .userDetailsService(this.userDetailsService)
            // 使用BCrypt进行密码的hash
            .passwordEncoder(passwordEncoder());
        // 默认超级用户
        auth.inMemoryAuthentication()
                .withUser("toor")
                .password(new BCryptPasswordEncoder().encode("nerv_toor_eva"))
                .roles("ADMIN");
    }

    @Bean
    public JwtAuthFilter authenticationTokenFilterBean() {
        return new JwtAuthFilter();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(CollUtil.isEmpty(evaConfig.getJwt().getCreditUrl())?Arrays.asList("*"): evaConfig.getJwt().getCreditUrl());
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(Arrays.asList("PUT", "DELETE", "GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setMaxAge(1800l);
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Headers",
                                                      "Access-Control-Allow-Methods",
                                                      "Access-Control-Expose-Headers",
                                                      "Access-Control-Allow-Origin",
                                                      "Access-Control-Max-Age",
                                                      "authorization",
                                                      "auth_token",
                                                      "xsrf-token",
                                                      "content-type",
                                                      "X-Frame-Options",
                                                      "Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
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
            .antMatchers(anonymous).permitAll()
            // 除上面外的所有请求全部需要鉴权认证
            .anyRequest().authenticated();

            httpSecurity.logout().logoutUrl("/auth/logout").logoutSuccessHandler(urlLogoutSuccessHandler);

            httpSecurity.exceptionHandling()
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(urlAccessDeniedHandler)
                        .and()
                        .addFilterAt(urlFilterSecurityInterceptor, FilterSecurityInterceptor.class)
                        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                        .addFilterBefore(new JwtUsernamePasswordAuthenticationFilter("/auth/login",
                                                                                          authenticationManager(),
                                                                                          urlAuthenticationSuccessHandler,
                                                                                          urlAuthenticationFailureHandler),
                                         UsernamePasswordAuthenticationFilter.class);

            // disable page caching
            httpSecurity
            .headers()
            .frameOptions().sameOrigin()  // required to set for H2 else H2 Console will be blank.
            .cacheControl();
    }

    @Override
    public void configure(WebSecurity web) {
        web
            .ignoring()
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