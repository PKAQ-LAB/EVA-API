package io.nerv.config;

import com.google.common.collect.Sets;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ApiDescriptionBuilder;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.Operation;
import springfox.documentation.service.ParameterType;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ApiListingScannerPlugin;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spring.web.readers.operation.CachingOperationNameGenerator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 手动添加swagger接口，如登录接口等
 */
@Component
public class SwaggerAddtion implements ApiListingScannerPlugin {
    /**
     * 实现此方法可手动添加ApiDescriptions
     *
     * @param context - Documentation context that can be used infer documentation context
     * @return List of {@link ApiDescription}
     * @see ApiDescription
     */
    @Override
    public List<ApiDescription> apply(DocumentationContext context) {

        var username = new RequestParameterBuilder()
                                             .description("用户名")
                                             .name("account")
                                             .in(ParameterType.BODY)
                                             .accepts(Collections.singleton(MediaType.APPLICATION_JSON))
                                             .query(q -> q.defaultValue("admin")
                                                    .model(modelSpecificationBuilder -> modelSpecificationBuilder.scalarModel(ScalarType.STRING)))
                                             .required(true).query(param -> param.model(model -> model.scalarModel(ScalarType.STRING)))
                                             .build();

        var pwd = new RequestParameterBuilder()
                                        .description("密码")
                                        .name("password")
                                        .in(ParameterType.BODY)
                                        .accepts(Collections.singleton(MediaType.APPLICATION_JSON))
                                        .query(q -> q.defaultValue("admin123")
                                                .model(modelSpecificationBuilder -> modelSpecificationBuilder.scalarModel(ScalarType.STRING)))
                                        .required(true).query(param -> param.model(model -> model.scalarModel(ScalarType.STRING)))
                                        .build();


        Operation usernamePasswordOperation = new OperationBuilder(new CachingOperationNameGenerator())
            .method(HttpMethod.POST)
            .summary("用户名密码登录")
            .notes("username/password登录")
            // 接收参数格式
            .consumes(Sets.newHashSet(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
            // 返回参数格式
            .produces(Sets.newHashSet(MediaType.APPLICATION_JSON_VALUE))
            .tags(Sets.newHashSet("登录"))
            .requestParameters(Collections.singleton(username))
            .requestParameters(Collections.singleton(pwd))
            .responses(Collections.singleton(
                new ResponseBuilder()
                            .code("200")
                            .description("请求成功")
                    .build()))
            .build();

        ApiDescription apiDescription = new ApiDescriptionBuilder(Comparator.comparingInt(Operation::getPosition))
                .description("Endpoint to request a jwt token")
                .groupName("AUTH")
                .hidden(false)
                .operations(Collections.singletonList(usernamePasswordOperation))
                .summary("login summary")
                .path("/auth/login")
                .build();


        return Collections.singletonList(apiDescription);
    }
    /**
     * 是否使用此插件
     * 
     * @param documentationType swagger文档类型
     * @return true 启用
     */
    @Override
    public boolean supports(DocumentationType documentationType) {
        return DocumentationType.SWAGGER_2.equals(documentationType);
    }
}