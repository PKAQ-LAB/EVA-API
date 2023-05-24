package io.nerv.core.mvc.response;

import io.nerv.core.enums.BizCode;
import io.nerv.core.enums.BizCodeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

/**
 * 返回对象
 *
 * @author PKAQ
 */
@Data
@Slf4j
@Accessors(chain = true)
@NoArgsConstructor
public class Response {

    private String code;

    private boolean success;

    private String message;

    private Object data;

    public Response(Boolean success, Object data) {
        this.data = data;
        this.success = success;
    }

    /**
     * 响应成功
     *
     * @return
     */
    public Response success() {
        this.success = true;
        this.code = BizCodeEnum.OPERATE_SUCCESS.getCode();

        return this;
    }

    /**
     * 响应成功
     *
     * @param data
     * @return
     */
    public Response success(Object data) {
        this.data = data;
        this.success = true;
        this.code = BizCodeEnum.OPERATE_SUCCESS.getCode();
        this.message = BizCodeEnum.OPERATE_SUCCESS.getMsg();

        return this;
    }

    /**
     * 响应成功
     *
     * @param data
     * @return
     */
    public Response success(Object data, String msg) {
        this.data = data;
        this.success = true;
        this.message = msg;
        this.code = BizCodeEnum.OPERATE_SUCCESS.getCode();

        return this;
    }

    /**
     * 响应成功
     *
     * @param data
     * @return
     */
    public Response success(Object data, BizCode msg) {
        this.data = data;
        this.success = true;
        this.message = msg.getMsg();
        this.code = msg.getCode();

        return this;
    }

    /**
     * 响应成功
     *
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

    public Response success(Object data, BizCode bizCode, Object... args) {
        this.data = data;
        this.success = true;
        this.message = MessageFormat.format(bizCode.getMsg(), null == args ? "" : args);
        this.code = bizCode.getCode();
        return this;
    }


    /**
     * 失败响应，自定义响应码和消息
     *
     * @param code
     * @return
     */
    public Response failure(String code, String message) {
        this.success = false;
        this.code = code;
        this.message = message;

        return this;
    }

    /**
     * 失败响应，自定义响应码和消息
     *
     * @param errorCodeEnum
     * @return
     */
    public Response failure(BizCode errorCodeEnum) {
        this.success = false;

        this.code = errorCodeEnum.getCode();

        this.message = MessageFormat.format("[{0}] {1}", errorCodeEnum.getCode(), errorCodeEnum.getMsg());

        return this;
    }

    public Response failure(BizCode errorCodeEnum, Object... args) {
        this.success = false;

        this.code = errorCodeEnum.getCode();

        this.message = MessageFormat.format("[" + code + "]" + errorCodeEnum.getMsg(), args);

        return this;
    }

    /**
     * 响应失败
     *
     * @param data
     * @return
     */
    public Response failure(String code, String message, Object data) {
        this.data = data;
        this.message = message;
        this.success = false;
        this.code = code;

        return this;
    }
}
