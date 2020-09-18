package io.nerv.config;

import io.nerv.handler.ResAccessDeniedHandler;
import io.nerv.handler.ResAuthenticationEntryPoint;
import io.nerv.handler.ResAuthenticationFailureHandler;
import io.nerv.handler.ResAuthenticationSuccessHandler;
import io.nerv.manager.PermissionAuthorizationManager;
import io.nerv.manager.ReactiveJwtAuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.server.WebFilter;

import java.security.KeyPair;

/**
 * 资源服务器配置
 */
@EnableWebFluxSecurity
public class ResourceServerConfig {

    private final String jks = "C:\\Users\\PKAQ\\jwtpub.jks";

    private final String pwd = "7u8i9o0p";

    private final String alias = "evapub";


    @Autowired
    private AuthConfig authConfig;

    @Autowired
    private PermissionAuthorizationManager permissionAuthorizationManager;

    private final static String Bearer = "Bearer";

    @Bean
    public SecurityWebFilterChain webFluxSecurityFilterChain(ServerHttpSecurity http) {

        ReactiveAuthenticationManager reactiveAuthenticationManager = new ReactiveJwtAuthenticationManager(tokenStore());

        ResAccessDeniedHandler resAccessDeniedHandler = new ResAccessDeniedHandler();
        ResAuthenticationEntryPoint resAuthenticationEntryPoint = new ResAuthenticationEntryPoint();

        //构建Bearer Token
        //请求参数强制加上 Authorization BEARER token
        http.addFilterAt((WebFilter) (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String access_tk = request.getQueryParams().getFirst("access_token");

            if( access_tk != null) {
                exchange.getRequest().mutate().headers(httpHeaders ->
                        httpHeaders.add(
                                "Authorization",
                                Bearer+" "+access_tk)
                );
            }
            return chain.filter(exchange);
        }, SecurityWebFiltersOrder.FIRST);



        ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchange = http.authorizeExchange();
        //无需进行权限过滤的请求路径
        //  authorizeExchange.matchers(EndpointRequest.toAnyEndpoint()).permitAll();
        //无需进行权限过滤的请求路径
        authorizeExchange.pathMatchers(authConfig.getAnonymous()).permitAll();

        http.authorizeExchange()
                .pathMatchers("/security/**").permitAll()
                .anyExchange().access(permissionAuthorizationManager)
                .and()
                //oauth2认证过滤器
                .addFilterAt(authenticationWebFilter(reactiveAuthenticationManager), SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling()
                .accessDeniedHandler(resAccessDeniedHandler)
                .authenticationEntryPoint(resAuthenticationEntryPoint);

        http.oauth2ResourceServer().jwt();
        http.csrf().disable();
        return http.build();
    }

    /**
     * 身份认证
     * @return
     * @param reactiveAuthenticationManager
     */
    public AuthenticationWebFilter authenticationWebFilter(ReactiveAuthenticationManager reactiveAuthenticationManager){
        AuthenticationWebFilter authenticationWebFilter= new AuthenticationWebFilter(reactiveAuthenticationManager);
        //登陆验证失败
        authenticationWebFilter.setAuthenticationFailureHandler(new ResAuthenticationFailureHandler());
        //认证成功
        authenticationWebFilter.setAuthenticationSuccessHandler(new ResAuthenticationSuccessHandler());
        return authenticationWebFilter;

    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }
    /**
     * JwtAccessTokenConverter
     * TokenEnhancer的子类，帮助程序在JWT编码的令牌值和OAuth身份验证信息之间进行转换。
     */

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter accessTokenConverter = new JwtAccessTokenConverter();
        //配置JWT使用的秘钥
        accessTokenConverter.setKeyPair(keyPair());
        return accessTokenConverter;
    }

    @Bean
    public KeyPair keyPair() {
        //从classpath下的证书中获取秘钥对
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new FileSystemResource(jks), pwd.toCharArray());
        return keyStoreKeyFactory.getKeyPair(alias, pwd.toCharArray());
    }

}