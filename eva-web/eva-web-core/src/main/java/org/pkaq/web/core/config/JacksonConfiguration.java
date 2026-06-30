package org.pkaq.web.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pkaq.core.util.json.JacksonObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
}
