package io.nerv.config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * @author PKAQ
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Eva API",
                version = "1.0",
                description = "springdoc-openAPI 接口说明文档",
                contact = @Contact(name = "PKAQ")
        ),
        security = @SecurityRequirement(name = "JWT"),
        externalDocs = @ExternalDocumentation(description = "参考文档",
                url = "https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations"
        )
)
@SecurityScheme(type = SecuritySchemeType.HTTP, name = "Authorization", scheme = "Bearer", in = SecuritySchemeIn.HEADER)
public class Swagger3Configuration {
//    @Bean
//    public OpenAPI customOpenAPI() {
//        StringSchema schema = new StringSchema();
//        return new OpenAPI()
//                .components(new Components().addParameters("myGlobalHeader", new HeaderParameter().required(true).name("My-Global-Header").description("My Global Header").schema(schema)));
//    }
//
//    @Bean
//    public OpenApiCustomiser customerGlobalHeaderOpenApiCustomiser() {
//        return openApi -> openApi.getPaths().values().stream().flatMap(pathItem -> pathItem.readOperations().stream())
//                .forEach(operation -> operation.addParametersItem(new HeaderParameter().$ref("#/components/parameters/myGlobalHeader")));
//    }
}
