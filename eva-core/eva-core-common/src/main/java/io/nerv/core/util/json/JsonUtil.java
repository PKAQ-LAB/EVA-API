package io.nerv.core.util.json;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nerv.core.exception.Exceptions;
import io.nerv.core.util.StringPool;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jackson工具类
 *
 * @author Chill
 */
@Slf4j
public class JsonUtil {
    private static ObjectMapper objectMapper = new JacksonObjectMapper();

    /**
     * 将对象序列化成json字符串
     *
     * @param value javaBean
     * @param <T>   T 泛型标记
     * @return jsonString json字符串
     */
    public static <T> String toJson(T value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 将对象序列化成 json byte 数组
     *
     * @param object javaBean
     * @return jsonString json字符串
     */
    public static byte[] toJsonAsBytes(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 将json反序列化成对象
     *
     * @param content   content
     * @param valueType class
     * @param <T>       T 泛型标记
     * @return Bean
     */
    public static <T> T parse(String content, Class<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 将json反序列化成对象
     *
     * @param content       content
     * @param typeReference 泛型类型
     * @param <T>           T 泛型标记
     * @return Bean
     */
    public static <T> T parse(String content, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(content, typeReference);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 将json byte 数组反序列化成对象
     *
     * @param bytes     json bytes
     * @param valueType class
     * @param <T>       T 泛型标记
     * @return Bean
     */
    public static <T> T parse(byte[] bytes, Class<T> valueType) {
        try {
            return objectMapper.readValue(bytes, valueType);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw Exceptions.unchecked(e);
        }
    }


    /**
     * 将json反序列化成对象
     *
     * @param bytes         bytes
     * @param typeReference 泛型类型
     * @param <T>           T 泛型标记
     * @return Bean
     */
    public static <T> T parse(byte[] bytes, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(bytes, typeReference);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 将json反序列化成对象
     *
     * @param in        InputStream
     * @param valueType class
     * @param <T>       T 泛型标记
     * @return Bean
     */
    public static <T> T parse(InputStream in, Class<T> valueType) {
        try {
            return objectMapper.readValue(in, valueType);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 将json反序列化成对象
     *
     * @param in            InputStream
     * @param typeReference 泛型类型
     * @param <T>           T 泛型标记
     * @return Bean
     */
    public static <T> T parse(InputStream in, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(in, typeReference);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 将json反序列化成List对象
     *
     * @param content      content
     * @param valueTypeRef class
     * @param <T>          T 泛型标记
     * @return List
     */
    public static <T> List<T> parseArray(String content, Class<T> valueTypeRef) {
        try {

            if (!StrUtil.startWithIgnoreCase(content, StringPool.BRACKET_START)) {
                content = StringPool.BRACKET_START + content + StringPool.BRACKET_END;
            }

            List<Map<String, Object>> list = objectMapper.readValue(content, new TypeReference<>() {
            });
            List<T> result = new ArrayList<>();
            for (Map<String, Object> map : list) {
                result.add(toPojo(map, valueTypeRef));
            }
            return result;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static Map<String, Object> toMap(String content) {
        try {
            return objectMapper.readValue(content, Map.class);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static <T> Map<String, T> toMap(String content, Class<T> valueTypeRef) {
        try {
            Map<String, Map<String, Object>> map = objectMapper.readValue(content, new TypeReference<>() {
            });
            Map<String, T> result = new HashMap<>(16);
            for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
                result.put(entry.getKey(), toPojo(entry.getValue(), valueTypeRef));
            }
            return result;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static <T> T toPojo(Map fromValue, Class<T> toValueType) {
        return objectMapper.convertValue(fromValue, toValueType);
    }

    /**
     * 将json字符串转成 JsonNode
     *
     * @param jsonString jsonString
     * @return jsonString json字符串
     */
    public static JsonNode readTree(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 将json字符串转成 JsonNode
     *
     * @param in InputStream
     * @return jsonString json字符串
     */
    public static JsonNode readTree(InputStream in) {
        try {
            return objectMapper.readTree(in);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 将json字符串转成 JsonNode
     *
     * @param content content
     * @return jsonString json字符串
     */
    public static JsonNode readTree(byte[] content) {
        try {
            return objectMapper.readTree(content);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 将json字符串转成 JsonNode
     *
     * @param jsonParser JsonParser
     * @return jsonString json字符串
     */
    public static JsonNode readTree(JsonParser jsonParser) {
        try {
            return objectMapper.readTree(jsonParser);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw Exceptions.unchecked(e);
        }
    }
}

