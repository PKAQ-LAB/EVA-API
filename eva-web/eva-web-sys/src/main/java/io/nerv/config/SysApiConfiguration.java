package io.nerv.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * swagger配置类
 * @author PKAQ
 * @Profile()
 */
@Configuration
@Profile({"!prod"})
public class SysApiConfiguration {
    @Bean
    public GroupedOpenApi sysApiGroup() {
        return GroupedOpenApi.builder()
                .group("系统管理")
                .pathsToMatch("/sys/**")
                .build();
    }
}
