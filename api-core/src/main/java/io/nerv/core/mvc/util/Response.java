package io.nerv.core.mvc.util;

import io.nerv.core.enums.HttpCodeEnum;
import io.nerv.core.enums.ResponseEnumm;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

@Data
/*
@Builder
*/
public class Response{
    private int status;
    private boolean success;
    private String message;
    private Object data;

    public Response() {

    }
    public Response(Boolean success, Object data) {
        this.data = data;
        this.success = success;
    }

    /**
     * 响应成功
     * @return
     */
    public Response success(){
        this.success = true;
        this.message = ResponseEnumm.OPERATE_SUCCESS.getName();
        this.status = HttpCodeEnum.QUERY_SUCCESS.getIndex();
        return this;
    }
    /**
     * 响应成功
     * @param data
     * @return
     */
    public Response success(Object data) {
        this.data = data;
        this.success = true;
        this.message = ResponseEnumm.OPERATE_SUCCESS.getName();
        this.status = HttpCodeEnum.QUERY_SUCCESS.getIndex();
        return this;
    }
    /**
     * 响应成功
     * @param data
     * @return
     */
    public Response success(Object data, String msg) {
        this.data = data;
        this.success = true;
        this.message = msg;
        this.status = HttpCodeEnum.QUERY_SUCCESS.getIndex();
        return this;
    }

    /**
     * 响应失败
     * @param data
     * @return
     */
    public Response failure(Object data) {
        this.data = data;
        this.message = ResponseEnumm.OPERATE_FAILED.getName();
        this.success = false;
        this.status = HttpCodeEnum.REQEUST_FAILURE.getIndex();
        return this;
    }

    /**
     * 失败响应,根据status设置statusText
     * @param status
     * @return
     */
    public Response failure(int status) {
        this.success = false;
        this.status = status;
        this.message = HttpCodeEnum.getName(status);
        return this;
    }

    /**
     * 失败响应，自定义响应码和消息
     * @param status
     * @return
     */
    public Response failure(int status, String message) {
        this.success = false;
        this.status = status;
        this.message = message;
        return this;
    }

    /**
     * 响应失败
     * @param data
     * @return
     */
    public Response failure(int status, String message, Object data) {
        this.data = data;
        this.message = message;
        this.success = false;
        this.status = HttpCodeEnum.REQEUST_FAILURE.getIndex();
        return this;
    }


    /**
     * 对data对象里对应的属性置空
     * @param values 需要为空的属性名
     * @return
     */
    public Response filter(Object data,String[] values) throws IllegalAccessException{
        if(data != null ) {
            for (String value : values) {

                Object fatherObject = data;
                String[] splitValue = value.split("\\.");

                //直接属性的话，判断什么类型然后直接置位空
                if (splitValue.length == 1) {
                    //判断是否是数组/集合
                    if (data.getClass().isArray() || data instanceof Collection) {
                        for (Object obj : (Collection) data) {
                            setNull(obj, value);
                        }
                    } else if (data instanceof Map) {
                        //判断是否是map集合
                        Map map = (Map) data;
                        map.put(value, null);
                    } else {
                        //javabean
                        setNull(data, value);
                    }
                    return success(data);
                }

                //链式属性的话，循环每一阶层的属性名
                for (int i = 0; i < splitValue.length; i++) {
                    String stepValue = splitValue[i];

                    //得到当前循环的子集的所有下层子集
                    int begin = value.indexOf(".", i + 1);
                    String[] sValue = {value.substring(begin + 1, value.length())};

                    //对当前对象判断
                    //如果是数组或集合,则循环属性值，把接下去的阶层属性置空
                    if (fatherObject.getClass().isArray() || fatherObject instanceof Collection) {
                        for (Object obj : (Collection) fatherObject) {
                            Field field = getField(obj, stepValue);
                            if (field != null) {
                                filter(field.get(obj), sValue);
                            }
                        }
                        break;
                    } else if (fatherObject instanceof Map) {
                        //如果是map集合的话，则得到map对应的key(下一阶层的属性名)的value对象，然后递归该对象
                        Map map = (Map) fatherObject;
                        filter(map.get(stepValue), sValue);
                        break;
                    } else {
                        Field field = getField(fatherObject, stepValue);
                        if (field != null) {
                            field.setAccessible(true);
                            filter(field.get(fatherObject), sValue);
                        }
                    }
                }
            }
        }
        return success(data);
    }

    /**
     * 给该对象的value属性赋值
     * @param object
     * @param value
     * @throws IllegalAccessException
     */
    public void setNull(Object object,String value)throws IllegalAccessException{
        Field field =getField(object,value);
        if(field != null){
            field.setAccessible(true);
            field.set(object,null);
        }
    }

    /**
     * 返回该对象的value属性
     * @param object
     * @param value
     * @return
     */
    public Field getField(Object object,String value){
        Field field = null;
        try{
            field=object.getClass().getDeclaredField(value);
        }catch (NoSuchFieldException e){
            try {
                field=object.getClass().getSuperclass().getDeclaredField(value);
            }catch (NoSuchFieldException e1){
                System.out.println(object+"对象没有"+value+"属性！！");
            }
        }
        return field;
    }
}
