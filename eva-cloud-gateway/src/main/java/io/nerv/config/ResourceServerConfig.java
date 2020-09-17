package io.nerv.config;

import io.nerv.handler.ResAccessDeniedHandler;
import io.nerv.handler.ResAuthenticationEntryPoint;
import io.nerv.handler.ResAuthenticationFailureHandler;
import io.nerv.handler.ResAuthenticationSuccessHandler;
import io.nerv.manager.AuthorizationManager;
import io.nerv.manager.TokenAuthenticationConverter;
import io.nerv.manager.TokenAuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.server.WebFilter;

/**
 * 资源服务器配置
 */
@Configuration
public class ResourceServerConfig {
    @Autowired
    private AuthConfig authConfig;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private AuthorizationManager authorizationManager;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity serverHttpSecurity) {

        // 认证处理器
        ReactiveAuthenticationManager tokenAuthenticationManager = new TokenAuthenticationManager(tokenStore);
        ResAuthenticationEntryPoint resAuthenticationEntryPoint = new ResAuthenticationEntryPoint();
        // 拒绝访问处理
        ResAccessDeniedHandler resAccessDeniedHandler = new ResAccessDeniedHandler();

        //构建Bearer Token
        //请求参数强制加上 Authorization BEARER token
        serverHttpSecurity.addFilterAt((WebFilter) (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String access_tk = request.getQueryParams().getFirst("access_token");

            if( access_tk != null) {
                exchange.getRequest().mutate().headers(httpHeaders ->
                        httpHeaders.add(
                                "Authorization",
                                OAuth2AccessToken.BEARER_TYPE+" "+access_tk)
                );
            }
            return chain.filter(exchange);
        }, SecurityWebFiltersOrder.FIRST);

        //身份认证
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(tokenAuthenticationManager);
        //登陆验证失败
        authenticationWebFilter.setAuthenticationFailureHandler(new ResAuthenticationFailureHandler());
        //认证成功
        authenticationWebFilter.setAuthenticationSuccessHandler(new ResAuthenticationSuccessHandler());

        //token转换器
        TokenAuthenticationConverter tokenAuthenticationConverter = new TokenAuthenticationConverter();
        tokenAuthenticationConverter.setAllowUriQueryParameter(true);
        authenticationWebFilter.setServerAuthenticationConverter(tokenAuthenticationConverter);

        serverHttpSecurity.addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchange = serverHttpSecurity.authorizeExchange();

        //无需进行权限过滤的请求路径
        authorizeExchange.matchers(EndpointRequest.toAnyEndpoint()).permitAll();
        //无需进行权限过滤的请求路径
        authorizeExchange.pathMatchers(authConfig.getAnonymous()).permitAll();

        //option 请求默认放行
        authorizeExchange
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                // 应用api权限控制
                .anyExchange().access(authorizationManager)
                //token 有效性控制
                .and()
                .exceptionHandling()
                .accessDeniedHandler(resAccessDeniedHandler)
                .authenticationEntryPoint(resAuthenticationEntryPoint)
                .and()
                .headers()
                .frameOptions()
                .disable()
                .and()
                .httpBasic().disable()
                .csrf().disable();
        return serverHttpSecurity.build();
    }
}