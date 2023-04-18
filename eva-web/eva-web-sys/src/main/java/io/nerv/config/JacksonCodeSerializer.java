package io.nerv.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import io.nerv.sys.annotation.Code;
import io.nerv.sys.dict.cache.DictCacheHelper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Objects;

/**
 * @author PKAQ
 */
@NoArgsConstructor
@AllArgsConstructor
public class JacksonCodeSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private String code;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String key = this.code;
        DictCacheHelper dictCacheHelper = SpringUtil.getBean(DictCacheHelper.class);
        if (null != dictCacheHelper && StrUtil.isNotBlank(value)) {
            value = dictCacheHelper.get(key).getOrDefault(value, value);
        }
        gen.writeString(value);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        // 为空直接跳过
        if (property != null) {
            // 非 String 类直接跳过
            if (Objects.equals(property.getType().getRawClass(), String.class)) {
                Code codeFilter = property.getAnnotation(Code.class);
                if (codeFilter == null) {
                    codeFilter = property.getContextAnnotation(Code.class);
                }
                // 如果能得到注解，就将注解的 value 传入
                if (codeFilter != null) {
                    return new JacksonCodeSerializer(codeFilter.value());
                }
            }
            return prov.findValueSerializer(property.getType(), property);
        }
        return prov.findNullValueSerializer(property);
    }
}