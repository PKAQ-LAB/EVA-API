package io.nerv.core.util;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class JsonUtil {
    @Autowired
    private ObjectMapper objectMapper;

    public String toJSONString(Object obj) {
        String jsonStr = "";
        try {
            jsonStr = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            jsonStr = "";
        } finally {
            return jsonStr;
        }
    }

    /**
     * 字符串转对象
     * @param jsonStr
     * @param clazz
     * @param <T>
     * @return
     */
    public <T>T parseObject(String jsonStr, Class<T> clazz){
        if (StrUtil.isBlank(jsonStr)){
            return null;
        }
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        try {
            return objectMapper.readValue(jsonStr, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
