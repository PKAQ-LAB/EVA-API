package io.nerv.core.mvc.vo;

import io.nerv.core.enums.BizCode;
import io.nerv.core.enums.BizCodeEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Accessors(chain = true)
public class Response{

    private String code;

    private boolean success;

    private String message;

    private String errorMessage;

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
        this.code = BizCodeEnum.OPERATE_SUCCESS.getIndex();

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
        this.code = BizCodeEnum.OPERATE_SUCCESS.getIndex();
        this.message = BizCodeEnum.OPERATE_SUCCESS.getName();

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
        this.code = BizCodeEnum.OPERATE_SUCCESS.getIndex();

        return this;
    }

    /**
     * 响应成功
     * @param data
     * @return
     */
    public Response success(Object data, BizCode msg) {
        this.data = data;
        this.success = true;
        this.message = msg.getName();
        this.code = msg.getIndex();

        return this;
    }

    /**
     * 响应成功
     * @param data
     * @return
     */
    public Response success(Object data, String msg, String code) {
        this.data = data;
        this.success = true;
        this.message = msg;
        this.code = code;
        return this;
    }


    /**
     * 失败响应，自定义响应码和消息
     * @param code
     * @return
     */
    public Response failure(String code, String message) {
        this.success = false;
        this.code = code;
        this.errorMessage = message;

        return this;
    }

    /**
     * 失败响应，自定义响应码和消息
     * @param errorCodeEnum
     * @return
     */
    public Response failure(BizCode errorCodeEnum) {
        this.success = false;

        this.code = errorCodeEnum.getIndex();

        this.errorMessage = String.format("[%s] %s",
                                     errorCodeEnum.getIndex(), errorCodeEnum.getName());

        return this;
    }

    /**
     * 响应失败
     * @param data
     * @return
     */
    public Response failure(String code, String message, Object data) {
        this.data = data;
        this.errorMessage = message;
        this.success = false;
        this.code = code;

        return this;
    }
}
