package org.pkaq.core.mvc.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.pkaq.core.enums.BizCode;
import org.pkaq.core.enums.BizCodeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 返回对象
 *
 * @author PKAQ
 */
@Data
@Slf4j
@Accessors(chain = true)
@NoArgsConstructor
public class Response<T> {
    private boolean success = true;

    private String code = "0000";

    private String message;

    // 业务代码
    @JsonIgnore
    private Object mtype;

    @JsonIgnore
    private Object[] args;

    private T data;

    public Response(Boolean success, T data) {
        this.data = data;
        this.success = success;
    }

    /**
     * 响应成功
     *
     * @return
     */
    public Response success() {
        this.mtype = BizCodeEnum.OPERATE_SUCCESS;
        return this;
    }

    /**
     * 响应成功
     *
     * @param data
     * @return
     */
    public Response<T> success(T data) {
        this.data = data;
        return this;
    }

    /**
     * 响应成功
     *
     * @return
     */
    public Response<T> success(String msg, Object... args) {
        this.message = msg;
        this.args = args;
        return this;
    }

    /**
     * 响应成功
     *
     * @param data
     * @return
     */
    public Response<T> success(T data, BizCode bizCode) {
        this.data = data;
        this.mtype = bizCode;

        return this;
    }


    /**
     * 响应成功
     *
     * @param data
     * @return
     */
    public Response<T> success(T data, String msg, String code) {
        this.data = data;
        this.message = msg;
        this.code = code;
        return this;
    }

    
    public Response<T> success(T data, BizCode bizCode, Object... args) {
        this.data = data;
        this.mtype = bizCode;
        this.args = args;
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
     * 失败处理
     *
     * @param msg
     * @return
     */
    public Response failure(String msg) {
        this.success = false;
        this.message = msg;
        return this;
    }

    /**
     * 失败响应，自定义响应码和消息
     *
     * @param bizCode
     * @return
     */
    
    public Response failure(BizCode bizCode) {
        this.success = false;
        this.mtype = bizCode;

        return this;
    }

    public Response failure(BizCode bizCode, T data) {
        this.success = false;

        this.mtype = bizCode;
        this.data = data;

        return this;
    }

    public Response<T> failure(BizCode bizCode, T data, Object... args) {
        this.success = false;

        this.mtype = bizCode;
        this.args = args;

        this.data = data;

        return this;
    }

    public Response<T> failure(String code, String msg, T data, Object... args) {
        this.success = false;
        this.code = code;
        this.message = msg;
        this.args = args;

        this.data = data;

        return this;
    }

    /**
     * 响应失败
     *
     * @param data
     * @return
     */
    public Response failure(String code, String message, T data) {
        this.data = data;
        this.message = message;
        this.success = false;
        this.code = code;

        return this;
    }
}
