package org.pkaq.core.auth.config;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.openapi.filter.AppKeyAuthenticationFilter;
import org.pkaq.core.auth.security.entrypoint.*;
import org.pkaq.core.auth.security.filter.JwtAuthFilter;
import org.pkaq.core.auth.security.provider.JwtUsernamePasswordAuthenticationFilter;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.util.ArrayUtils;
import org.pkaq.core.util.CollUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * @author PKAQ
 * 1.必须保证prePostEnabled开启 否则@PreAuthorize("hasRole('ROLE_ADMIN')")无效
 * 2.启用EnableConfigurationProperties以使ConfigurationProperties生效
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity()
public class WebSecurityConfig {

    private final EvaConfig evaConfig;
    private final UrlAuthenticationSuccessHandler urlAuthenticationSuccessHandler;
    private final UrlAuthenticationFailureHandler urlAuthenticationFailureHandler;
    private final UrlLogoutSuccessHandler urlLogoutSuccessHandler;
    private final UrlAccessDeniedHandler urlAccessDeniedHandler;
    private final UnauthorizedHandler unauthorizedHandler;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtAuthFilter jwtAuthFilter;
    private final AppKeyAuthenticationFilter appKeyAuthenticationFilter;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    /**
     * 跨域配置
     *
     * @return
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        boolean hasCreditUrl = !CollUtils.isEmpty(evaConfig.getJwt().getCreditUrl());
        configuration.setAllowedOrigins(hasCreditUrl ? evaConfig.getJwt().getCreditUrl() : List.of("*"));
        configuration.setAllowCredentials(hasCreditUrl);
        configuration.setAllowedMethods(Arrays.asList("PUT", "DELETE", "GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setMaxAge(1800L);
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

    @Bean
    public SecurityFilterChain httpSecurityConfigure(HttpSecurity httpSecurity) {

        httpSecurity.cors(Customizer.withDefaults())
                // 关闭csrf 由于使用的是JWT，这里不需要csrf
                .csrf(AbstractHttpConfigurer::disable)
                .headers(header -> {
                    //允许加载iframe内容 X-Frame-Options
                    header.frameOptions(frame -> frame.sameOrigin());
                    header.cacheControl(Customizer.withDefaults());
                    // 适配IE
                    header.addHeaderWriter(new StaticHeadersWriter("P3P",
                            "CP='CAO IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT'"));
                    header.xssProtection(Customizer.withDefaults());
                })
                // 基于token，不需要session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(Customizer.withDefaults());

        // 允许匿名访问的url
        String[] anonymousPaths = evaConfig.getAuth().getAnonymous();
        if (anonymousPaths != null && anonymousPaths.length > 0) {
            httpSecurity.authorizeHttpRequests(auth -> auth.requestMatchers(anonymousPaths).permitAll());
        }

        httpSecurity.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

        httpSecurity.logout(logout -> logout.logoutUrl(contextPath + "/auth/logout").logoutSuccessHandler(urlLogoutSuccessHandler));

        httpSecurity.exceptionHandling(ex ->
                ex.authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(urlAccessDeniedHandler));

        if (evaConfig.getAuth().isOpenApiEnabled()) {
            httpSecurity.addFilterBefore(appKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        if (evaConfig.getAuth().isJwtEnabled()) {
            httpSecurity
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                    .addFilterBefore(new JwtUsernamePasswordAuthenticationFilter(contextPath + "/auth/login",
                                    authenticationConfiguration.getAuthenticationManager(),
                                    urlAuthenticationSuccessHandler,
                                    urlAuthenticationFailureHandler),
                            UsernamePasswordAuthenticationFilter.class);
        }

//            @Secured( value={"ROLE_ANONYMOUS"})
        httpSecurity.anonymous(anonymous -> anonymous.authorities("ROLE_ANONYMOUS"));

        return httpSecurity.build();
    }

//    @Bean
//    SecurityFilterChain decisionConfig(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests((authorize) -> authorize
//                        .anyRequest().access(urlAccessDecisionManager)
//        );
////        http.authorizeHttpRequests().anyRequest().access(urlAccessDecisionManager);
//
//        return http.build();
//    }
    /**
     * 禁止 JwtAuthFilter 被Spring Boot自动注册为Servlet Filter
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * 禁止 AppKeyAuthenticationFilter 被Spring Boot自动注册为Servlet Filter
     */
    @Bean
    public FilterRegistrationBean<AppKeyAuthenticationFilter> appKeyAuthenticationFilterRegistration(
            AppKeyAuthenticationFilter filter) {
        FilterRegistrationBean<AppKeyAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * 忽略静态资源
     */
    @Bean
    public WebSecurityCustomizer webSecurityConfigure() {
        return web -> {
            var staticPath = new String[]{
                    "/",
                    "/static/**",
                    "/*.html",
                    "/*.xls",
                    "/*.xlsx",
                    "/*.doc",
                    "/*.docx",
                    "/*.pdf",
                    "/favicon.ico",
                    "/*/*.html",
                    "/*/*.css",
                    "/*/*.js",
                    "/*/swagger-resources/**",
                    "/*/api-docs/**"
            };

            String[] paths = staticPath;
            if (null != evaConfig.getAuth().getWebstatic()) {
                paths = ArrayUtils.addAll(evaConfig.getAuth().getWebstatic(), staticPath);
            }

            web.ignoring()
                    // allow anonymous resource requests
                    .requestMatchers(HttpMethod.GET, paths)
                    .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
        };
    }
}

