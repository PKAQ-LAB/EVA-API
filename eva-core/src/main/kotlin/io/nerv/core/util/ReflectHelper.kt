package io.nerv.core.util

import io.nerv.exception.ReflectException
import org.slf4j.LoggerFactory
import java.lang.reflect.Field

object ReflectHelper {
    val log = LoggerFactory.getLogger(this.javaClass)
    /**
     * 给object对象的fieldName属性赋value值
     * @param object
     * @param fieldName
     */
    fun setValue(`object`: Any, fieldName: String, value: Any?) {
        try {
            val field = getField(`object`, fieldName)
            if (field != null) {
                field.isAccessible = true
                field[`object`] = value
            }
        } catch (e: IllegalAccessException) {
            throw ReflectException("把" + `object`.javaClass + "对象的" + fieldName + "属性值设置成" + value + "失败")
        }
    }

    /**
     * 返回object对象的fieldName属性对象
     * @param object
     * @param fieldName
     * @return
     */
    fun getField(`object`: Any, fieldName: String): Field? {
        var field: Field? = null
        try {
            field = `object`.javaClass.getDeclaredField(fieldName)
        } catch (e: NoSuchFieldException) {
            try {
                field = `object`.javaClass.superclass.getDeclaredField(fieldName)
            } catch (e1: NoSuchFieldException) {
                log.error(`object`.toString() + "对象没有" + fieldName + "属性")
            }
        }
        return field
    }

    /**
     * 把object.fieldName属性设置空
     * @param object
     * @param fieldName
     */
    fun setNull(`object`: Any, fieldName: String) {
        //三次判断
        //fieldName是object对象的集合/数组属性
        if (`object`.javaClass.isArray || `object` is Collection<*>) {
            for (obj in `object` as Collection<*>) {
                if (obj != null) {
                    setValue(obj, fieldName, null)
                }
            }
        } else if (`object` is Map<*, *>) {
            //判断是否是map集合
            val map = `object` as MutableMap<Any, Any?>
            map[fieldName] = null
        } else {
            //fieldName是object对象的一个普通属性
            setValue(`object`, fieldName, null)
        }
    }
}