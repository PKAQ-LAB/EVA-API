package io.nerv.config;

import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 添加全局的请求头参数
 *
 * @author PKAQ
 */
@Component
public class OpenApiGlobalHeader {

    @Bean
    public OpenApiCustomizer customerGlobalHeaderOpenApiCustomiser() {
        return openApi -> openApi.getPaths().values().stream().flatMap(pathItem -> pathItem.readOperations().stream())
                .forEach(operation -> {
                    StringSchema schema = new StringSchema();
                    schema.setEnum(Arrays.asList("api", "iphone", "ipad", "android", "win", "mac"));
                    operation.addParametersItem(
                            new HeaderParameter()
                                    .in("header")
                                    .required(false)
                                    .schema(new StringSchema())
                                    .description("设备ID")
                                    .name("deviceID"));

                    operation.addParametersItem(
                            new HeaderParameter()
                                    .in("header")
                                    .required(true)
                                    .schema(schema)
                                    .description("设备类型")
                                    .name("deviceType"));

                    operation.addParametersItem(
                            new HeaderParameter()
                                    .in("header")
                                    .required(true)
                                    .schema(new StringSchema())
                                    .description("应用版本")
                                    .name("version"));
                });
    }
}
