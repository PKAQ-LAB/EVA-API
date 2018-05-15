package org.pkaq.config;

import org.pkaq.security.entrypoint.AuthEntryPoint;
import org.pkaq.security.filter.JwtAuthFilter;
import org.pkaq.security.jwt.JwtConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

    @Autowired
    private AuthEntryPoint authEntryPoint;
    /** Spring会自动寻找同样类型的具体类注入，这里就是JwtUserDetailsServiceImpl了**/
    @Autowired
    private UserDetailsService userDetailsService;

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
    public JwtAuthFilter authenticationTokenFilterBean() throws Exception {
        return new JwtAuthFilter();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").
                         allowedOrigins("*")
                        .allowedMethods("GET", "HEAD", "POST","PUT", "DELETE", "OPTIONS")
                        .allowCredentials(false)
                        .maxAge(3600);
            }
        };
    }
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // 关闭csrf 由于使用的是JWT，我们这里不需要csrf
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(authEntryPoint)
                .and()
                //允许加载iframe内容 X-Frame-Options
                .headers().frameOptions().disable()
                .and()
                // 基于token，所以不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
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
                // 限定访问IP
                .antMatchers("/druid/**").hasIpAddress("127.0.0.1")
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated();

        // 添加JWT filter
        httpSecurity.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);

        // 禁用缓存
        httpSecurity.headers().cacheControl();
    }
}