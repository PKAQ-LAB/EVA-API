package io.nerv.core.web.advice;

import io.nerv.core.annotation.JsonFilter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * 序列化对象时过滤某些属性
 *
 * @author PKAQ
 */
@Aspect
@Component
@Slf4j
public class JsonFilterAdvice {
    /**
     * 定义切入点
     */
    @Pointcut("@annotation(io.nerv.core.annotation.JsonFilter)")
    public void annotation() {
    }
    /**
     * 以jsonFilter注解为切入点切入方法，序列化对象时过滤某些属性
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("annotation()")
    public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
        //得到Controller层返回的对象
        Object retVal = pjp.proceed();

        //得到方法上的注解
        Class clazzz = pjp.getTarget().getClass();
        Method mm = clazzz.getDeclaredMethod(pjp.getSignature().getName(), pjp.getArgs().getClass());
        JsonFilter jsonFilter = mm.getAnnotation(JsonFilter.class);
        if (jsonFilter == null || jsonFilter.exclude().length < 1) {
            return null;
        } else {
            //操作对象，使得注解里对应的属性值为空
            nullVlueFeild(retVal, jsonFilter.exclude());
        }
        return retVal;
    }

    /**
     * 把对象的属性置空
     * @param returnValue
     * @param values
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */

    private void nullVlueFeild(Object returnValue, String[] values) throws NoSuchFieldException, IllegalAccessException {
        for (String value : values) {
            //根据.号分级
            String[] splitValue = value.split("\\.");
            if (splitValue.length == 1) {
                //顶层属性,直接设置属性值为空
                setNull(returnValue, value);
                continue;
            }
            //表示上一层的属性对象的值
            Object fatherObj = returnValue;
            //表示当前循环的属性对象的值
            Object object;
            for (int i = 0; i < splitValue.length - 1; i++) {

                //得到当前对象的当前属性的值
                String param = splitValue[i];
                Field field = null;
                try {
                    field = fatherObj.getClass().getDeclaredField(param);
                } catch (NoSuchFieldException e) {
                    field = fatherObj.getClass().getSuperclass().getDeclaredField(param);
                }
                if (field == null) {
                    continue;
                }
                field.setAccessible(true);
                object = field.get(fatherObj);


                //如果是最后一个对象 即最后一个点之前的属性值 c（a.b.c.d）,直接设置值
                if (splitValue.length - 2 == i) {
                    setNull(object, splitValue[i + 1]);
                }

                //得到当前循环的子集的所有下层子集
                int begin = value.indexOf(".", i + 1);
                String[] sValue = {value.substring(begin + 1, value.length())};

                //判断是否是数组/集合
                if (object.getClass().isArray() || object instanceof Collection) {
                    for (Object obj : (Collection) object) {
                        //把当前属性的对象和所有下层子集传到方法中
                        nullVlueFeild(obj, sValue);
                    }
                    break;
                } else if (object instanceof Map) {
                    //判断是否是map集合
                    Map map = (Map) object;
                    //map对象点之后即map的key 第二个点即该key对应的value里的属性
                    String last = sValue[0];
                    last = last.substring(last.indexOf(".") + 1, last.length());
                    sValue[0] = last;
                    nullVlueFeild(map.get(splitValue[i + 1]), sValue);
                    break;
                }
                //下一级的父对象为当前这一级
                fatherObj = object;
            }
        }
    }

    /**
     * 根据对象类型把该对象的value置位空
     *
     * @param object
     * @param value
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public void setNull(Object object, String value) throws NoSuchFieldException, IllegalAccessException {
        //判断是否是数组/集合
        if (object.getClass().isArray() || object instanceof Collection) {
            for (Object obj : (Collection) object) {
                nul(obj, value);
            }
        } else if (object instanceof Map) {
            //判断是否是map集合
            Map map = (Map) object;
            map.put(value, null);
        } else {
            //javabean
            nul(object, value);
        }
    }

    /**
     * 把object的value属性置位空
     *
     * @param object
     * @param value
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public void nul(Object object, String value) throws NoSuchFieldException, IllegalAccessException {
        Field field = null;

        for (Object obj : (Collection) object) {
            try {
                field = obj.getClass().getDeclaredField(value);
            } catch (NoSuchFieldException e) {
                field = obj.getClass().getSuperclass().getDeclaredField(value);
            }
            field.setAccessible(true);
            field.set(obj, null);

        }
    }

}

 