package org.pkaq.core.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.pkaq.core.util.DatePatterns;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author PKAQ
 */
public class JacksonObjectMapper extends ObjectMapper implements Serializable {
    private static final Locale CHINA = Locale.CHINA;

    public JacksonObjectMapper() {
        super();
        //设置地点为中国
        super.setLocale(CHINA);

        //设置为中国上海时区
        super.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));

        // 禁用将日期序列化为时间戳
        super.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 配置多态类型验证器 (安全性考虑)
        // 限制只允许特定基类型，防止反序列化漏洞
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        super.setDateFormat(new SimpleDateFormat(DatePatterns.NORM_DATETIME_PATTERN, Locale.CHINA));
        //序列化处理
        super.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        super.configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true);
        super.findAndRegisterModules();
        //失败处理
        super.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        super.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //单引号处理
        super.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        //反序列化时，属性不存在的兼容处理s
        super.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //序列化时，日期的统一格式
        SimpleModule longModule = new SimpleModule();
        longModule.addSerializer(Long.class, ToStringSerializer.instance);
        longModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        super.registerModule(longModule);
        // 注册Java 8时间模块
        super.registerModule(new JavaTimeModule());
        super.findAndRegisterModules();
    }

    @Override
    public ObjectMapper copy() {
        return super.copy();
    }
}
