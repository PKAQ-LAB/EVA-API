package io.nerv.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
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
public class SysAPIConfiguration {
    @Bean
    public OpenAPI sysApi() {
        return new OpenAPI()
                .info(new Info().title("系统管理")
                        .description("系统管理Springdoc接口文档")
                        .version("v1.0.0"));
    }
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("系统管理")
                .pathsToMatch("/sys/**")
                .build();
    }
}
