package io.nerv.cache.config;

import cn.hutool.core.collection.CollUtil;
import com.google.common.base.Predicate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

/**
 * swagger配置类
 * Datetime: 2016-11-25 11:44
 * @author PKAQ
 * @Profile()
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.profiles", name = "active", havingValue = "dev")
public class SysAPIConfiguration {
    @Bean
    public Docket sysApi() {
        Predicate<RequestHandler> sys = RequestHandlerSelectors.basePackage("io.nerv");

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("系统管理")
                .securitySchemes(CollUtil.toList(
                        new ApiKey("Authorization", "Authorization", "header")))
                .apiInfo(apiInfo())
                .select()
                .apis(sys)
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("EVA-Sys API接口文档")
                .description("http://pkaq.org")
                .termsOfServiceUrl("http://pkaq.org/")
                .contact(new Contact("PKAQ","http://pkaq.org","pkaq@msn.com"))
                .version("1.0")
                .build();
    }
}
