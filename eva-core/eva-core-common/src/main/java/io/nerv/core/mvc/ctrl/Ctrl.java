package io.nerv.core.mvc.ctrl;

import io.nerv.core.enums.BizCode;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.response.Response;
import io.nerv.core.util.I18NHelper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller 基类- 无默认CRUD方法
 *
 * @author S.PKAQ
 */
@Getter
public abstract class Ctrl {

    @Autowired
    protected I18NHelper i18NHelper;

    /**
     * 根据编码获取国际化字符串
     *
     * @param code
     * @return
     */
    protected String locale(String code) {
        return this.i18NHelper.getMessage(code);
    }

    /**
     * 操作成功
     *
     * @return
     */
    protected <T> Response<T> success() {
        return new Response<T>().success(null);
    }

//    protected <T> Response<T> exclude(Object data, String... values){
//        <T> Response<T> response = new Response<>();
//        response.exclude(data, values);
//        return response;
//    }

    /**
     * 返回成功结果
     *
     * @param data
     * @return
     */
    protected <T> Response<T> success(T data) {
        return new Response<T>().success(data);
    }

    /**
     * 返回成功结果
     *
     * @param data
     * @return
     */
    protected <T> Response<T> success(T data, String msg) {
        return new Response<T>().success(data, msg);
    }

    /**
     * 返回成功结果
     *
     * @param data
     * @param bizCode
     * @return
     */
    protected <T> Response<T> success(T data, BizCode bizCode) {
        return new Response<T>().success(data, bizCode.getMsg());
    }

    /**
     * 返回失败结果
     *
     * @param failCode
     * @return
     */
    protected <T> Response<T> failure(BizCode failCode) {
        return new Response<T>().failure(failCode);
    }

    /**
     * 返回失败结果
     *
     * @param failCode
     * @param args
     * @return
     */
    protected <T> Response<T> failure(BizCode failCode, Object... args) {
        return new Response<T>().failure(failCode, args);
    }

    /**
     * 返回失败结果
     *
     * @param failCode
     * @return
     */
    protected <T> Response<T> failure(String failCode, String msg) {
        return new Response<T>().failure(failCode, msg);
    }

    /**
     * 返回失败结果
     *
     * @return
     */
    protected <T> Response<T> failure() {
        return new Response<T>().failure(BizCodeEnum.SERVER_ERROR);
    }
}
