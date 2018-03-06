package org.pkaq.core.util;

import cn.hutool.core.lang.Console;
import lombok.Data;
import org.pkaq.core.enums.HttpCodeEnum;

@Data
public class Response{
    private int statusCode;
    private boolean success;
    private String message;
    private Object data;

    private transient final String OPERATE_SUCCESS = "操作成功";
    private transient final String OPERATE_FAILED = "操作失败";

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
        this.message = this.OPERATE_SUCCESS;
        this.statusCode = HttpCodeEnum.QUERY_SUCCESS.getIndex();
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
        this.message = this.OPERATE_SUCCESS;
        this.statusCode = HttpCodeEnum.QUERY_SUCCESS.getIndex();
        return this;
    }

    /**
     * 响应失败
     * @param data
     * @return
     */
    public Response failure(Object data) {
        this.data = data;
        this.message = this.OPERATE_FAILED;
        this.success = false;
        this.statusCode = HttpCodeEnum.REQEUST_FAILURE.getIndex();
        return this;
    }

    /**
     * 失败响应,根据statusCode设置message
     * @param statusCode
     * @return
     */
    public Response failure(int statusCode) {
        this.success = false;
        this.statusCode = statusCode;
        this.message = HttpCodeEnum.getName(statusCode);
        return this;
    }

    /**
     * 失败响应，自定义响应码和消息
     * @param statusCode
     * @param message
     * @return
     */
    public Response failure(int statusCode, String message) {
        this.success = false;
        this.statusCode = statusCode;
        this.message = message;
        return this;
    }

    /**
     * 响应失败
     * @param data
     * @return
     */
    public Response failure(int statusCode, String message, Object data) {
        this.data = data;
        this.message = message;
        this.success = false;
        this.statusCode = HttpCodeEnum.REQEUST_FAILURE.getIndex();
        return this;
    }
}
