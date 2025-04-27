package org.pkaq.core.mvc.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.enums.BizCode;
import org.pkaq.core.enums.BizCodeEnum;

/**
 * 统一响应对象
 *
 * @param <T> 响应数据类型
 * @author PKAQ
 */
@Data
@Slf4j
@Accessors(chain = true)
@NoArgsConstructor
public class Response<T> {

    private boolean success = true;
    // 默认成功状态码
    private String code = "0000";
    private String message;
    // 业务代码
    @JsonIgnore
    private Object mtype;
    // 格式化参数
    @JsonIgnore
    private Object[] args;
    // 响应数据
    private T data;

    /**
     * 静态方法：创建成功的响应对象
     *
     * @param <T> 响应数据类型
     * @return 成功的响应对象
     */
    public static <T> Response<T> success() {
        log.info("Creating success response with default values.");
        return new Response<T>().setSuccess(true).setMtype(BizCodeEnum.OPERATE_SUCCESS);
    }

    /**
     * 静态方法：创建成功的响应对象并设置数据
     *
     * @param data 响应数据
     * @param <T>  响应数据类型
     * @return 成功的响应对象
     */
    public static <T> Response<T> success(T data) {
        log.info("Creating success response with data: {}", data);
        return new Response<T>().setSuccess(true).setData(data);
    }

    /**
     * 静态方法：创建成功的响应对象并设置消息和参数
     *
     * @param msg  消息
     * @param args 格式化参数
     * @param <T>  响应数据类型
     * @return 成功的响应对象
     */
    public static <T> Response<T> success(String msg, Object... args) {
        log.info("Creating success response with message: {}, args: {}", msg, args);
        return new Response<T>().setSuccess(true).setMessage(msg).setArgs(args);
    }

    /**
     * 静态方法：创建成功的响应对象并设置数据与业务代码
     *
     * @param data    响应数据
     * @param bizCode 业务代码
     * @param <T>     响应数据类型
     * @return 成功的响应对象
     */
    public static <T> Response<T> success(T data, BizCode bizCode) {
        log.info("Creating success response with data: {}, bizCode: {}", data, bizCode);
        return new Response<T>().setSuccess(true).setData(data).setMtype(bizCode);
    }

    /**
     * 静态方法：创建成功的响应对象并设置数据、消息和状态码
     *
     * @param data 响应数据
     * @param msg  消息
     * @param code 状态码
     * @param <T>  响应数据类型
     * @return 成功的响应对象
     */
    public static <T> Response<T> success(T data, String msg, String code) {
        log.info("Creating success response with data: {}, message: {}, code: {}", data, msg, code);
        return new Response<T>().setSuccess(true).setData(data).setMessage(msg).setCode(code);
    }

    /**
     * 静态方法：创建成功的响应对象并设置数据、业务代码和参数
     *
     * @param data    响应数据
     * @param bizCode 业务代码
     * @param args    格式化参数
     * @param <T>     响应数据类型
     * @return 成功的响应对象
     */
    public static <T> Response<T> success(T data, BizCode bizCode, Object... args) {
        log.info("Creating success response with data: {}, bizCode: {}, args: {}", data, bizCode, args);
        return new Response<T>().setSuccess(true).setData(data).setMtype(bizCode).setArgs(args);
    }

    /**
     * 静态方法：创建失败的响应对象并设置状态码和消息
     *
     * @param code    状态码
     * @param message 消息
     * @param <T>     响应数据类型
     * @return 失败的响应对象
     */
    public static <T> Response<T> failure(String code, String message) {
        log.warn("Creating failure response with code: {}, message: {}", code, message);
        return new Response<T>().setSuccess(false).setCode(code).setMessage(message);
    }

    /**
     * 静态方法：创建失败的响应对象并设置消息
     *
     * @param message 消息
     * @param <T>     响应数据类型
     * @return 失败的响应对象
     */
    public static <T> Response<T> failure(String message) {
        log.warn("Creating failure response with message: {}", message);
        return new Response<T>().setSuccess(false).setMessage(message);
    }

    /**
     * 静态方法：创建失败的响应对象并设置业务代码
     *
     * @param bizCode 业务代码
     * @param <T>     响应数据类型
     * @return 失败的响应对象
     */
    public static <T> Response<T> failure(BizCode bizCode) {
        log.warn("Creating failure response with bizCode: {}", bizCode);
        return new Response<T>().setSuccess(false).setMtype(bizCode);
    }

    /**
     * 静态方法：创建失败的响应对象并设置业务代码和数据
     *
     * @param bizCode 业务代码
     * @param data    响应数据
     * @param <T>     响应数据类型
     * @return 失败的响应对象
     */
    public static <T> Response<T> failure(BizCode bizCode, T data) {
        log.warn("Creating failure response with bizCode: {}, data: {}", bizCode, data);
        return new Response<T>().setSuccess(false).setMtype(bizCode).setData(data);
    }

    /**
     * 静态方法：创建失败的响应对象并设置业务代码、数据和参数
     *
     * @param bizCode 业务代码
     * @param data    响应数据
     * @param args    格式化参数
     * @param <T>     响应数据类型
     * @return 失败的响应对象
     */
    public static <T> Response<T> failure(BizCode bizCode, T data, Object... args) {
        log.warn("Creating failure response with bizCode: {}, data: {}, args: {}", bizCode, data, args);
        return new Response<T>().setSuccess(false).setMtype(bizCode).setData(data).setArgs(args);
    }

    /**
     * 静态方法：创建失败的响应对象并设置状态码、消息、数据和参数
     *
     * @param code    状态码
     * @param message 消息
     * @param data    响应数据
     * @param args    格式化参数
     * @param <T>     响应数据类型
     * @return 失败的响应对象
     */
    public static <T> Response<T> failure(String code, String message, T data, Object... args) {
        log.warn("Creating failure response with code: {}, message: {}, data: {}, args: {}", code, message, data, args);
        return new Response<T>().setSuccess(false).setCode(code).setMessage(message).setData(data).setArgs(args);
    }

    /**
     * 静态方法：创建失败的响应对象并设置状态码、消息和数据
     *
     * @param code    状态码
     * @param message 消息
     * @param data    响应数据
     * @param <T>     响应数据类型
     * @return 失败的响应对象
     */
    public static <T> Response<T> failure(String code, String message, T data) {
        log.warn("Creating failure response with code: {}, message: {}, data: {}", code, message, data);
        return new Response<T>().setSuccess(false).setCode(code).setMessage(message).setData(data);
    }
}