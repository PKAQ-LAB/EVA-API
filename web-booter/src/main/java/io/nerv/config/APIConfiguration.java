package io.nerv.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * swagger配置类
 * @author PKAQ
 * @Profile()
 */
@Configuration
@Profile({"!prod"})
public class APIConfiguration {
    @Bean
    public OpenApiCustomiser openApiCustomiser(){
        var pi = new PathItem();
        pi.description("用户登录")
                .summary("概括")
                .post(new Operation().tags(List.of("用户登录")).operationId("login").summary("登录"));
        return openApi -> openApi.getPaths().addPathItem("/auth/login", pi);
    }

    @Bean
    public GroupedOpenApi authApiGroup() {
        return GroupedOpenApi.builder()
                .group("访问中心")
                .pathsToMatch("/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi actuatorApiGroup() {
        return GroupedOpenApi.builder()
                .group("actuator")
                .pathsToMatch("/actuator/**")
                .build();
    }

    @Bean
    public GroupedOpenApi allApiGroup() {
        return GroupedOpenApi.builder()
                .group("全部接口")
                .pathsToMatch("/**")
                .build();
    }
}
