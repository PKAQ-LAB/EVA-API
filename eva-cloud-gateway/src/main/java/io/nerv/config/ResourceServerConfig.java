package io.nerv.config;

import io.nerv.constant.AuthConstant;
import io.nerv.handle.UrlAuthenticationDeniedHandler;
import io.nerv.handle.UrlAuthenticationFailureHandler;
import io.nerv.handle.UrlAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 资源服务器配置
 */
@Configuration
@EnableWebFluxSecurity
@EnableResourceServer
public class ResourceServerConfig {
    @Autowired
    private AuthConfig authConfig;

    @Autowired
    private AuthorizationManager authorizationManager;

    @Autowired
    private UrlAuthenticationSuccessHandler urlAuthenticationSuccessHandler;

    @Autowired
    private UrlAuthenticationDeniedHandler urlAuthenticationDeniedHandler;

    @Autowired
    private UrlAuthenticationFailureHandler urlAuthenticationFailureHandler;


    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.oauth2ResourceServer().jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter());

        //自定义处理JWT请求头过期或签名错误的结果
        //http.oauth2ResourceServer().authenticationEntryPoint(urlAuthenticationFailureHandler);

        //对白名单路径，直接移除JWT请求头
//        http.addFilterBefore(ignoreUrlsRemoveJwtFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        http.authorizeExchange()
                // 白名单
                .pathMatchers(authConfig.getAnonymous())
                .permitAll()
                //鉴权管理器配置
                .anyExchange()
                .access(authorizationManager)
                .and()
                .httpBasic()
                .and()
                .formLogin().loginPage("/auth/login")
                //认证成功
                .authenticationSuccessHandler(urlAuthenticationSuccessHandler)
                .and()
                //登陆验证失败
                //.authenticationFailureHandler(authenticationFaillHandler)
                //.and()
                .exceptionHandling()
                //处理未授权
                .accessDeniedHandler(urlAuthenticationDeniedHandler)
                //处理未认证
//                .authenticationEntryPoint(urlAuthenticationFailureHandler)
                .and()
                .csrf()
                .disable();
        return http.build();
    }

    @Bean
    public Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(AuthConstant.AUTHORITY_PREFIX);
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(AuthConstant.AUTHORITY_CLAIM_NAME);
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}