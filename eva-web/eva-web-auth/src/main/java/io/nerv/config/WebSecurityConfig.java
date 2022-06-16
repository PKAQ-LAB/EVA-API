package io.nerv.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import io.nerv.core.util.JsonUtil;
import io.nerv.properties.EvaConfig;
import io.nerv.security.entrypoint.*;
import io.nerv.security.filter.JwtAuthFilter;
import io.nerv.security.provider.JwtUsernamePasswordAuthenticationFilter;
import io.nerv.security.provider.UrlFilterSecurityInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
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
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Value("${eva.security.anonymous}")
    private String[] anonymous;

    @Value("${eva.security.webstatic}")
    private String[] webstatic;

    private final EvaConfig evaConfig;

    private final JsonUtil jsonUtil;

    private final UrlAuthenticationSuccessHandler urlAuthenticationSuccessHandler;

    private final UrlAuthenticationFailureHandler urlAuthenticationFailureHandler;

    private final UrlLogoutSuccessHandler urlLogoutSuccessHandler;

    private final UrlAccessDeniedHandler urlAccessDeniedHandler;

    private final UnauthorizedHandler unauthorizedHandler;

    private final UrlFilterSecurityInterceptor urlFilterSecurityInterceptor;

    private final AuthenticationConfiguration authenticationConfiguration;

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public JwtAuthFilter authenticationTokenFilterBean() {
        return new JwtAuthFilter();
    }

    /**
     * 跨域配置
     * @return
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(CollUtil.isEmpty(evaConfig.getJwt().getCreditUrl())?Arrays.asList("*"): evaConfig.getJwt().getCreditUrl());
        configuration.setAllowCredentials(false);
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

    @Bean
    public SecurityFilterChain httpSecurityConfigure(HttpSecurity httpSecurity) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);

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

            if (evaConfig.getResourcePermission().isEnable()){
                httpSecurity.addFilterAt(urlFilterSecurityInterceptor, FilterSecurityInterceptor.class);
            }

            httpSecurity.exceptionHandling()
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(urlAccessDeniedHandler)
                        .and()
                        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                        .addFilterBefore(new JwtUsernamePasswordAuthenticationFilter("/auth/login",
                                                                                          authenticationConfiguration.getAuthenticationManager(),
                                                                                          urlAuthenticationSuccessHandler,
                                                                                          urlAuthenticationFailureHandler,
                                                                                          jsonUtil),
                                         UsernamePasswordAuthenticationFilter.class);

//            @Secured( value={"ROLE_ANONYMOUS"})
            httpSecurity.anonymous().authorities("ROLE_ANONYMOUS");

            // disable page caching
            httpSecurity
            .headers()
            .frameOptions().sameOrigin()  // required to set for H2 else H2 Console will be blank.
            .cacheControl();

            return httpSecurity.build();
    }


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
                  "/**/*.html",
                  "/**/*.css",
                  "/**/*.js",
                  "/**/swagger-resources/**",
                  "/**/api-docs/**"
          };

          if (null != webstatic ){
              paths = ArrayUtil.addAll(webstatic,staticPath);
          }

          web.ignoring()
              // allow anonymous resource requests
              .and()
              .ignoring()
              .antMatchers(
                      HttpMethod.GET,
                      paths
              );
      };
    }

}