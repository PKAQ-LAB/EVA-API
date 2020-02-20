package io.nerv.config;

import cn.hutool.core.collection.CollUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * swagger配置类
 * Datetime: 2016-11-25 11:44
 * @author PKAQ
 * @Profile()
 */
@Configuration
@EnableSwagger2
@Profile("dev")
public class SwaggerConfiguration {
    @Bean
    public Docket createRestApi() {
        List<Parameter> pars = new ArrayList<>();

        var device = new ParameterBuilder();
        var version = new ParameterBuilder();

        device.name("device").description("设备类型")
                .modelRef(new ModelRef("string")).parameterType("header")
                //header中的ticket参数非必填，传空也可以
                .required(false).build();
        pars.add(device.build());

        version.name("version").description("应用版本")
                .modelRef(new ModelRef("string")).parameterType("header")
                //header中的ticket参数非必填，传空也可以
                .required(false).build();
        pars.add(version.build());

        return new Docket(DocumentationType.SWAGGER_2)
                .globalOperationParameters(pars)
                .securitySchemes(CollUtil.toList(
                        new ApiKey("Authorization", "Authorization", "header")))
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("io.nerv"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("RESTful APIs")
                .description("http://pkaq.org")
                .termsOfServiceUrl("http://pkaq.org/")
                .contact(new Contact("pkaq","http://pkaq.org","pkaq@msn.com"))
                .version("1.0")
                .build();
    }
}
