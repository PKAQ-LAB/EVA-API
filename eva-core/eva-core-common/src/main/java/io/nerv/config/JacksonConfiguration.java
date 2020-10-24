package io.nerv.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.text.SimpleDateFormat;

/**
 * 处理日期问题
 */
@Configuration
public class JacksonConfiguration {
    @Bean
    MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper mapper = new ObjectMapper();
                     mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
                     mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        converter.setObjectMapper(mapper);
        return converter;
    }
}
