package io.nerv.config;

import io.nerv.manager.PermissionAuthorizationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 资源服务器配置
 */
@EnableWebFluxSecurity
public class ResourceServerConfig {
    @Autowired
    private PermissionAuthorizationManager permissionAuthorizationManager;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        http.authorizeExchange()
                .pathMatchers("/security/**").permitAll()
                .anyExchange().access(permissionAuthorizationManager);

        http.oauth2ResourceServer().jwt();
        http.csrf().disable();
        return http.build();
    }
}