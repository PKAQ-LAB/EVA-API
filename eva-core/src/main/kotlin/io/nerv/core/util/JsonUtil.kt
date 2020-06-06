package io.nerv.core.util

import cn.hutool.core.util.StrUtil
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JsonUtil {
    @Autowired
    private val objectMapper: ObjectMapper? = null
    fun toJSONString(obj: Any?): String {
        var jsonStr = ""
        jsonStr = try {
            objectMapper!!.writeValueAsString(obj)
        } catch (e: JsonProcessingException) {
            ""
        } finally {
            return jsonStr
        }
    }

    /**
     * 字符串转对象
     * @param jsonStr
     * @param clazz
     * @param <T>
     * @return
    </T> */
    fun <T> parseObject(jsonStr: String?, clazz: Class<T>?): T? {
        if (StrUtil.isBlank(jsonStr)) {
            return null
        }
        objectMapper!!.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        return try {
            objectMapper.readValue(jsonStr, clazz)
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
            null
        }
    }
}