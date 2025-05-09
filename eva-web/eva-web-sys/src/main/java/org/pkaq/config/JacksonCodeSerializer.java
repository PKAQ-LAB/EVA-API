package org.pkaq.config;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.NoArgsConstructor;
import org.pkaq.sys.annotation.Code;
import org.pkaq.sys.dict.cache.DictCacheHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * 自定义 Jackson 序列化器，用于在 JSON 序列化时根据 @Code 注解对字段值进行字典翻译。
 * 示例：
 * public class User {
 *     @author PKAQ
 * {@code @Code("user_type")}
 *     private String type; // 如 "1" -> "管理员"
 * }
 */
@NoArgsConstructor
public class JacksonCodeSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private static final Logger log = LoggerFactory.getLogger(JacksonCodeSerializer.class);

    private String codeKey;

    public JacksonCodeSerializer(String codeKey) {
        this.codeKey = codeKey;
    }

    /**
     * 实际执行序列化的方法，若存在字典配置，则替换为字典值。
     */
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (!StringUtils.hasText(value)) {
            gen.writeString(value);
            return;
        }

        try {
            DictCacheHelper dictCacheHelper = SpringUtil.getBean(DictCacheHelper.class);
            if (dictCacheHelper != null && StringUtils.hasText(codeKey)) {
                String translated = dictCacheHelper.get(codeKey).getOrDefault(value, value);
                gen.writeString(translated);
            } else {
                gen.writeString(value);
            }
        } catch (Exception e) {
            log.warn("Failed to translate code value for key: {}, value: {}", codeKey, value, e);
            gen.writeString(value);
        }
    }

    /**
     * 根据字段注解动态创建带上下文信息的序列化器实例。
     */
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property == null) {
            // 根据需要返回默认序列化器
            return prov.findNullValueSerializer(null);
        }

        if (!Objects.equals(property.getType().getRawClass(), String.class)) {
            return prov.findValueSerializer(property.getType(), property);
        }

        Code annotation = property.getAnnotation(Code.class);
        if (annotation == null) {
            annotation = property.getContextAnnotation(Code.class);
        }

        if (annotation != null && StringUtils.hasText(annotation.value())) {
            return new JacksonCodeSerializer(annotation.value());
        }

        return prov.findValueSerializer(property.getType(), property);
    }
}