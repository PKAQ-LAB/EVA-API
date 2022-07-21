package io.nerv.config;

import io.nerv.jwt.JwtTokenEnhancer;
import io.nerv.service.UserDetailsServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

/**
 * 认证服务器配置
 * /oauth/authorize：授权端点
 * /oauth/token：获取令牌端点
 * /oauth/confirm_access：用户确认授权提交端点
 * /oauth/error：授权服务错误信息端点
 * /oauth/check_token：用于资源服务访问的令牌解析端点
 * /oauth/token_key：提供公有密匙的端点，如果你使用JWT令牌的话
 *
 */
@AllArgsConstructor
@Configuration
@EnableAuthorizationServer
public class Oauth2ServerConfig extends AuthorizationServerConfigurerAdapter {
    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenEnhancer jwtTokenEnhancer;
    private final RedisConnectionFactory redisConnectionFactory;
    /**
     * 客户端信息配置
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetailsService());
/**
//        持久化client信息
        clients.jdbc(dataSource)
                .withClient("api-admin")
                .secret(passwordEncoder.encode("123456"))
                .scopes("all")
                .authorizedGrantTypes("password", "refresh_token")
                .accessTokenValiditySeconds(3600 * 24)
                .refreshTokenValiditySeconds(3600 * 24 * 7)
                .and().build();
 **/
/**
//         通过内存方式设置
        clients.inMemory()
                .withClient("api-admin")
                .secret(passwordEncoder.encode("123456"))
                .scopes("all")
                .authorizedGrantTypes("password", "refresh_token")
                .accessTokenValiditySeconds(3600 * 24)
                .refreshTokenValiditySeconds(3600 * 24 * 7)
                // 配置第二个客户端
                .and()
                .withClient("AuthConstant.PORTAL_CLIENT_ID")
                .secret(passwordEncoder.encode("123456"))
                .scopes("all")
                .authorizedGrantTypes("password", "refresh_token")
                .accessTokenValiditySeconds(3600 * 24)
                .refreshTokenValiditySeconds(3600 * 24 * 7);
**/
    }

    /**
     * 配置授权（authorization）以及令牌（token）
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {

//        endpoints.pathMapping("/oauth/token","/login");

        TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
        List<TokenEnhancer> delegates = new ArrayList<>();
        delegates.add(jwtTokenEnhancer);
        delegates.add(accessTokenConverter());

        //配置JWT的内容增强器
        enhancerChain.setTokenEnhancers(delegates);
        //使用密码模式需要配置
        endpoints.authenticationManager(authenticationManager)
                //指定token存储到redis
                .tokenStore(redisTokenStore())
                // 重复使用：access token过期刷新时， refresh token过期时间未改变，仍以初次生成的时间为准
                // 非重复使用：access token过期刷新时， refresh token过期时间延续，在refresh token有效期内刷新便永不失效达到
                .reuseRefreshTokens(false)
                // 获取令牌时校验用户名密码
                .userDetailsService(userDetailsService)
                .accessTokenConverter(accessTokenConverter())
                .tokenEnhancer(enhancerChain);
    }

    public RedisTokenStore redisTokenStore(){
        return new RedisTokenStore(redisConnectionFactory);
    }

    /**
     * 从数据库获取client配置信息
     * @return
     */
    public ClientDetailsService clientDetailsService(){
        var clientDetail = new JdbcClientDetailsService(dataSource);
            clientDetail.setPasswordEncoder(passwordEncoder);

        return clientDetail;
    }

    /**
     * 允许表单认证
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
//        RequestAuthenticationFilter endpointFilter = new RequestAuthenticationFilter(security);
//        endpointFilter.afterPropertiesSet();
//        endpointFilter.setAuthenticationEntryPoint(authenticationEntryPoint());
//        security.addTokenEndpointAuthenticationFilter(endpointFilter);

        security
            // 允许所有资源服务器访问公钥端点（/oauth/token_key）
            // 只允许验证用户访问令牌解析端点（/oauth/check_token）
            .tokenKeyAccess("permitAll()")
            // isAuthenticated
            .checkTokenAccess("isAuthenticated()")
            // 允许客户端发送表单来进行权限认证来获取令牌
            //如果关闭的话默认会走basic认证 即clientId和clientSecret组合起来base加密后放在http header中传递
            .allowFormAuthenticationForClients();
    }

    /**
     * 使用非对称加密算法对token签名
     */
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setKeyPair(keyPair());
        return jwtAccessTokenConverter;
    }

    /**
     * 从classpath下的密钥库中获取密钥对(公钥+私钥)
     */
    @Bean
    public KeyPair keyPair() {
        //从classpath下的证书中获取秘钥对
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("eva-jwt.jks"), "7u8i9o0p".toCharArray());
        return keyStoreKeyFactory.getKeyPair("eva-jwt", "7u8i9o0p".toCharArray());
    }
}
//
//
//keytool -genkey -alias eva-jwt -keyalg RSA -keysize 1024 -keystore eva-jwt.jks -validity 365 -keypass 7u8i9o0p -storepass 7u8i9o0p