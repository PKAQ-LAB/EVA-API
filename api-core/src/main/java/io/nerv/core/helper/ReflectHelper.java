package io.nerv.core.helper;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

@Slf4j
public class ReflectHelper {

    /**
     * 给object对象的fieldName属性赋value值
     * @param object
     * @param fieldName
     * @throws IllegalAccessException
     */
    public static void setValue(Object object,String fieldName,Object value)throws IllegalAccessException{
        Field field =getField(object,fieldName);
        if(field != null){
            field.setAccessible(true);
            field.set(object,value);
        }
    }

    /**
     * 返回object对象的fieldName属性对象
     * @param object
     * @param fieldName
     * @return
     */
    public static Field getField(Object object,String fieldName){
        Field field = null;
        try{
            field=object.getClass().getDeclaredField(fieldName);
        }catch (NoSuchFieldException e){
            try {
                field=object.getClass().getSuperclass().getDeclaredField(fieldName);
            }catch (NoSuchFieldException e1){
                log.error(object+"对象没有"+fieldName+"属性");
            }
        }
        return field;
    }

    /**
     *  把object.fieldName属性设置空
     * @param object
     * @param fieldName
     */
    public static void setNull(Object object,String fieldName) throws IllegalAccessException{
        //三次判断
        //fieldName是object对象的集合/数组属性
        if (object.getClass().isArray() || object instanceof Collection) {
            for (Object obj : (Collection) object) {
                setValue(obj, fieldName,null);
            }
        }
        //fieldName是object对象的map属性
        else if (object instanceof Map) {
            //判断是否是map集合
            Map map = (Map) object;
            map.put(fieldName, null);
        } else {
            //fieldName是object对象的一个普通属性
            setValue(object, fieldName,null);
        }
    }
}