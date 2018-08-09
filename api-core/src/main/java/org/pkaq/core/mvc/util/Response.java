package org.pkaq.core.mvc.util;

import lombok.Data;
import org.pkaq.core.enums.HttpCodeEnum;
import org.pkaq.core.enums.ResponseEnumm;

@Data
public class Response{
    private int status;
    private boolean success;
    private String statusText;
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
        this.statusText = ResponseEnumm.OPERATE_SUCCESS.getName();
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
        this.statusText = ResponseEnumm.OPERATE_SUCCESS.getName();
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
        this.statusText = ResponseEnumm.OPERATE_FAILED.getName();
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
        this.statusText = HttpCodeEnum.getName(status);
        return this;
    }

    /**
     * 失败响应，自定义响应码和消息
     * @param status
     * @param statusText
     * @return
     */
    public Response failure(int status, String statusText) {
        this.success = false;
        this.status = status;
        this.statusText = statusText;
        return this;
    }

    /**
     * 响应失败
     * @param data
     * @return
     */
    public Response failure(int status, String statusText, Object data) {
        this.data = data;
        this.statusText = statusText;
        this.success = false;
        this.status = HttpCodeEnum.REQEUST_FAILURE.getIndex();
        return this;
    }
}
