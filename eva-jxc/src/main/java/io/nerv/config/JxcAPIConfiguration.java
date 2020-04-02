package io.nerv.config;

import cn.hutool.core.collection.CollUtil;
import io.nerv.core.docs.SwaggerHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * swagger配置类
 * Datetime: 2016-11-25 11:44
 * @author PKAQ
 * @Profile()
 */
@Configuration
@Profile("dev")
public class JxcAPIConfiguration {
    @Bean
    public Docket jxcApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("进销存管理")
                .globalOperationParameters(SwaggerHelper.getHeadPars())
                .securitySchemes(CollUtil.toList(
                        new ApiKey("Authorization", "Authorization", "header")))
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("io.nerv.web.jxc"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("EVA-JXC API接口文档")
                .description("http://pkaq.org")
                .termsOfServiceUrl("http://pkaq.org/")
                .contact(new Contact("PKAQ","http://pkaq.org","pkaq@msn.com"))
                .version("1.0")
                .build();
    }
}
