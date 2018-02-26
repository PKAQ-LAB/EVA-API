package org.pkaq.core.util;

import lombok.Data;
import org.pkaq.core.enums.HttpCodeEnum;

@Data
public class Response{
    private int code;
    private boolean status;
    private String msg;
    private Object data;

    private final String OPERATE_SUCCESS = "操作成功";
    private final String OPERATE_FAILED = "操作失败";

    public Response() {

    }
    public Response(Boolean status, Object data) {
        this.data = data;
        this.status = status;
    }

    /**
     * 响应成功
     * @return
     */
    public Response success(){
        this.status = true;
        this.msg = this.OPERATE_SUCCESS;
        this.code = HttpCodeEnum.QUERY_SUCCESS.getIndex();
        return this;
    }
    /**
     * 响应成功
     * @param data
     * @return
     */
    public Response success(Object data) {
        this.data = data;
        this.status = true;
        this.msg = this.OPERATE_SUCCESS;
        this.code = HttpCodeEnum.QUERY_SUCCESS.getIndex();
        return this;
    }

    /**
     * 响应失败
     * @param data
     * @return
     */
    public Response failure(Object data) {
        this.data = data;
        this.msg = this.OPERATE_FAILED;
        this.status = false;
        this.code = HttpCodeEnum.REQEUST_FAILURE.getIndex();
        return this;
    }

    /**
     * 失败响应,根据code设置msg
     * @param code
     * @return
     */
    public Response failure(int code) {
        this.status = false;
        this.code = code;
        this.msg = HttpCodeEnum.getName(code);
        return this;
    }

    /**
     * 响应失败
     * @param data
     * @return
     */
    public Response failure(int code, String msg, Object data) {
        this.data = data;
        this.msg = this.msg;
        this.status = false;
        this.code = HttpCodeEnum.REQEUST_FAILURE.getIndex();
        return this;
    }
}
