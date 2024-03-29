package io.nerv.core.auth.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import io.nerv.core.auth.security.entrypoint.*;
import io.nerv.core.auth.security.filter.JwtAuthFilter;
import io.nerv.core.auth.security.provider.DynamiclAccessDecisionManager;
import io.nerv.core.auth.security.provider.JwtUsernamePasswordAuthenticationFilter;
import io.nerv.core.properties.EvaConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
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
    @Value("${eva.security.anonymous}")
    private String[] anonymous;
    @Value("${eva.security.webstatic}")
    private String[] webstatic;
    @Autowired(required = false)
    private DynamiclAccessDecisionManager urlAccessDecisionManager;

    /**
     * 跨域配置
     *
     * @return
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(CollUtil.isEmpty(evaConfig.getJwt().getCreditUrl()) ? List.of("*") : evaConfig.getJwt().getCreditUrl());
        configuration.setAllowCredentials(false);
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
    public SecurityFilterChain httpSecurityConfigure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors(Customizer.withDefaults())
                // 关闭csrf 由于使用的是JWT，我们这里不需要csrf
                .csrf(AbstractHttpConfigurer::disable)
                .headers(header -> {
                    //允许加载iframe内容 X-Frame-Options
                    header.frameOptions(frame -> {
                        frame.disable();
                        frame.sameOrigin();
                    });
                    header.cacheControl(Customizer.withDefaults());
                    // 适配IE
                    header.addHeaderWriter(new StaticHeadersWriter("P3P", "CP='CAO IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT'"));
                    header.xssProtection(Customizer.withDefaults());
                })
                // 基于token，所以不需要session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(Customizer.withDefaults());

        // 允许匿名访问的url
        httpSecurity.authorizeHttpRequests(auth -> auth.requestMatchers(anonymous).permitAll());

        if (null != urlAccessDecisionManager) {
            httpSecurity.authorizeHttpRequests(auth -> auth.anyRequest().access(urlAccessDecisionManager));
        } else {
            httpSecurity.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
        }

        httpSecurity.logout(logout -> {
            logout.logoutUrl("/auth/logout").logoutSuccessHandler(urlLogoutSuccessHandler);
        });

        httpSecurity.exceptionHandling(ex ->
                ex.authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(urlAccessDeniedHandler));

        httpSecurity
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtUsernamePasswordAuthenticationFilter("/auth/login",
                                authenticationConfiguration.getAuthenticationManager(),
                                urlAuthenticationSuccessHandler,
                                urlAuthenticationFailureHandler),
                        UsernamePasswordAuthenticationFilter.class);

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
     * 虽然登录请求可以被所有人访问，但是不能放在这里（而应该通过允许匿名访问的方式来给请求放行）。
     * 如果放在这里，登录请求将不走 SecurityContextPersistenceFilter 过滤器，也就意味着不会将登录用户信息存入 session，
     * 进而导致后续请求无法获取到登录用户信息。
     */
    @Bean
    public WebSecurityCustomizer webSecurityConfigure() {
        return web -> {
            String[] paths = null;
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

            if (null != webstatic) {
                paths = ArrayUtil.addAll(webstatic, staticPath);
            }

            web.ignoring()
                    // allow anonymous resource requests
                    .requestMatchers(HttpMethod.GET, paths)
                    .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
        };
    }
}