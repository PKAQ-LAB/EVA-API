package io.nerv.core.mvc.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.nerv.core.enums.HttpCodeEnum;
import io.nerv.core.enums.ResponseEnumm;
import io.nerv.core.helper.ReflectHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

@Data
@Slf4j
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
    public Response exclude(Object data,String[] values) throws IllegalAccessException{
        Object thisObject = data;
        if(data != null ) {
            for (String value : values) {

                //如果对象是Ipage对象，则fatherObject等于实际传给前台的对象即records集合。
                if (data instanceof IPage) {
                    thisObject = ((IPage) data).getRecords();
                }
                String[] splitValue = value.split("\\.");

                //thisObject的直接属性的话，判断什么类型然后直接置位空
                if (splitValue.length == 1) {
                    //对thisObject的splitValue[0]属性赋空值
                    ReflectHelper.setNull(thisObject,splitValue[0]);
                }else {
                    //链式属性的话，循环每一阶层的属性名
                    for (int i = 0; i < splitValue.length; i++) {
                        String stepValue = splitValue[i];

                        //得到当前循环的子集的所有下层子集
                        int begin = value.indexOf(".", i + 1);
                        String[] sValue = {value.substring(begin + 1, value.length())};

                        //对当前对象判断
                        //如果是数组或集合,则循环属性值，把接下去的阶层属性置空
                        if (thisObject.getClass().isArray() || thisObject instanceof Collection) {
                            for (Object obj : (Collection) thisObject) {
                                Field field = ReflectHelper.getField(obj, stepValue);
                                if (field != null) {
                                    exclude(field.get(obj), sValue);
                                }
                            }
                            break;
                        } else if (thisObject instanceof Map) {
                            //如果是map集合的话，则得到map对应的key(下一阶层的属性名)的value对象，然后递归该对象
                            Map map = (Map) thisObject;
                            exclude(map.get(stepValue), sValue);
                            break;
                        } else {
                            Field field = ReflectHelper.getField(thisObject, stepValue);
                            if (field != null) {
                                field.setAccessible(true);
                                exclude(field.get(thisObject), sValue);
                            }
                        }
                    }
                }
            }
        }
        this.setData(thisObject);

        return this;
    }


}
