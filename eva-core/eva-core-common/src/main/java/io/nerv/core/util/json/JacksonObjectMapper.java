package io.nerv.core.util.json;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author PKAQ
 */
public class JacksonObjectMapper extends ObjectMapper {
    private static final long serialVersionUID = 3576819881530616393L;

    private static final Locale CHINA = Locale.CHINA;

    public JacksonObjectMapper() {
        super();
        //设置地点为中国
        super.setLocale(CHINA);

        //去掉默认的时间戳格式
        super.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        //设置为中国上海时区
        super.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
        //序列化时，日期的统一格式
        super.setDateFormat(new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN, Locale.CHINA));
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
        //日期格式化
        super.registerModule(new JavaTimeModule());
        super.findAndRegisterModules();
    }

    @Override
    public ObjectMapper copy() {
        return super.copy();
    }
}