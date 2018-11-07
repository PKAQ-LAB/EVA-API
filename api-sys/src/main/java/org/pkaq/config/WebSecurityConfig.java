package org.pkaq.config;

import org.pkaq.security.entrypoint.AuthEntryPoint;
import org.pkaq.security.filter.EntryPointUnauthorizedHandler;
import org.pkaq.security.filter.JwtAuthFilter;
import org.pkaq.security.filter.RestAccessDeniedHandler;
import org.pkaq.security.jwt.JwtConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * @author PKAQ
 * 1.必须保证prePostEnabled开启 否则@PreAuthorize("hasRole('ROLE_ADMIN')")无效
 * 2.启用EnableConfigurationProperties以使ConfigurationProperties生效
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtConfig.class)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Autowired
    private AuthEntryPoint authEntryPoint;

    @Autowired
    private EntryPointUnauthorizedHandler entryPointUnauthorizedHandler;

    @Autowired
    private RestAccessDeniedHandler restAccessDeniedHandler;
    /** Spring会自动寻找同样类型的具体类注入，这里就是JwtUserDetailsServiceImpl了**/
    @Autowired
    @Qualifier("jwtUserDetailsServiceImpl")
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public JwtConfig jwtConfig(){
        return new JwtConfig();
    }

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                // 设置UserDetailsService
                .userDetailsService(this.userDetailsService)
                // 使用BCrypt进行密码的hash
                .passwordEncoder(passwordEncoder());
    }

    /**装载BCrypt密码编码器**/
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthFilter authenticationTokenFilterBean() {
        return new JwtAuthFilter();
    }


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("PUT", "DELETE", "GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Headers",
                                                      "Access-Control-Allow-Methods",
                                                      "Access-Control-Allow-Origin",
                                                      "Access-Control-Max-Age",
                                                      "authorization",
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
            .exceptionHandling().authenticationEntryPoint(authEntryPoint)
            .and()
            //允许加载iframe内容 X-Frame-Options
            .headers().frameOptions().disable()
            .and()
            // 基于token，所以不需要session
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and();

        if ("dev".equalsIgnoreCase(activeProfile)){
            httpSecurity.authorizeRequests().anyRequest().permitAll();
        } else {
            httpSecurity.authorizeRequests()
                    // 允许对于网站静态资源的无授权访问
                    .antMatchers(
                            HttpMethod.GET,
                            "/",
                            "/*.html",
                            "/favicon.ico",
                            "/**/*.html",
                            "/**/*.css",
                            "/**/*.js",
                            "/webjars/**",
                            "/swagger-resources/**",
                            "/*/api-docs"
                    ).permitAll()
                    // 对于获取token的rest api要允许匿名访问
                    .antMatchers("/auth/login").permitAll()
                    .antMatchers("/api/auth/login").permitAll()
                    // 除上面外的所有请求全部需要鉴权认证
                    // FIXME 此处配置会导致@PermitAll @DenyAll注解无效
                    .anyRequest().authenticated();
        }
        // 添加JWT filter
        httpSecurity.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        httpSecurity.exceptionHandling()
                    .authenticationEntryPoint(entryPointUnauthorizedHandler)
                    .accessDeniedHandler(restAccessDeniedHandler);
        // 禁用缓存
        httpSecurity.headers().cacheControl();
    }
}