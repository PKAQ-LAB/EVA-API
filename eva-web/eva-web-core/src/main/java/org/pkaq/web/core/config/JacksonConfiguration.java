package org.pkaq.web.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pkaq.core.util.json.JacksonObjectMapper;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * Web JSON 序列化配置。
 *
 * @author PKAQ
 */
@Configuration
public class JacksonConfiguration {

    /**
     * 统一 Web 响应的 ObjectMapper，避免 Long 类型 ID 在前端发生精度丢失。
     *
     * @return Jackson 对象映射器
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new JacksonObjectMapper();
    }

    /**
     * Spring Boot 4 Web MVC 使用 Jackson 3 的 JsonMapper，需要单独注册 Long 转字符串规则。
     *
     * @return Jackson 3 JSON 映射器定制器
     */
    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> {
            SimpleModule longModule = new SimpleModule("LongToStringModule");
            longModule.addSerializer(Long.class, ToStringSerializer.instance);
            longModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
            builder.addModule(longModule);
        };
    }
}
