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
import org.springframework.security.oauth2.config.annotation.builders.JdbcClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
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
        // 使用数据库存储的client信息设置
        clients.configure(jdbcClientDetailsServiceBuilder());
        // 通过内存方式设置
//        clients.inMemory()
//                .withClient("api-admin")
//                .secret(passwordEncoder.encode("123456"))
//                .scopes("all")
//                .authorizedGrantTypes("password", "refresh_token")
//                .accessTokenValiditySeconds(3600 * 24)
//                .refreshTokenValiditySeconds(3600 * 24 * 7)
//                // 配置第二个客户端
//                .and()
//                .withClient("AuthConstant.PORTAL_CLIENT_ID")
//                .secret(passwordEncoder.encode("123456"))
//                .scopes("all")
//                .authorizedGrantTypes("password", "refresh_token")
//                .accessTokenValiditySeconds(3600 * 24)
//                .refreshTokenValiditySeconds(3600 * 24 * 7);
    }

    /**
     * 配置授权（authorization）以及令牌（token）
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
        List<TokenEnhancer> delegates = new ArrayList<>();
        delegates.add(jwtTokenEnhancer);
        delegates.add(accessTokenConverter());

        //配置JWT的内容增强器
        enhancerChain.setTokenEnhancers(delegates);
                //使用密码模式需要配置
        endpoints.authenticationManager(authenticationManager)
                //指定token存储到redis
                .tokenStore(new RedisTokenStore(redisConnectionFactory))
                // 重复使用：access token过期刷新时， refresh token过期时间未改变，仍以初次生成的时间为准
                // 非重复使用：access token过期刷新时， refresh token过期时间延续，在refresh token有效期内刷新便永不失效达到
                .reuseRefreshTokens(false)
                // 刷新令牌授权包含对用户信息的检查
                .userDetailsService(userDetailsService)
                .accessTokenConverter(accessTokenConverter())
                .tokenEnhancer(enhancerChain);
    }

    @Bean
    public JdbcClientDetailsServiceBuilder jdbcClientDetailsServiceBuilder() {
        return new JdbcClientDetailsServiceBuilder()
                .dataSource(dataSource)
                .passwordEncoder(passwordEncoder);
    }

    /**
     * 允许表单认证
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.allowFormAuthenticationForClients();
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