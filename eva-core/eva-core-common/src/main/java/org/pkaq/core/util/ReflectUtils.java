package org.pkaq.core.util;

import java.lang.reflect.Field;

/**
 * 反射工具类
 */
public final class ReflectUtils {

    private ReflectUtils() {}
    /**
     * 获取对象指定字段的值（支持私有字段）
     *
     * @param obj 对象实例
     * @param field Field 对象
     * @return 字段值；如果获取失败则返回 null
     */
    public static Object getFieldValue(Object obj, Field field) {
        if (obj == null || field == null) {
            return null;
        }
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * 获取对象指定字段的值（支持私有字段）
     *
     * @param obj 对象实例
     * @param fieldName 字段名
     * @return 字段值；如果获取失败则返回 null
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null || fieldName == null || fieldName.isEmpty()) {
            return null;
        }
        try {
            Field field = getField(obj.getClass(), fieldName);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 递归查找字段（包含父类字段）
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
