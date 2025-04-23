package org.pkaq.core.mvc.ctrl;

import lombok.Getter;
import org.pkaq.core.enums.BizCode;
import org.pkaq.core.enums.BizCodeEnum;
import org.pkaq.core.i18n.I18NHelper;
import org.pkaq.core.mvc.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller 基类- 无默认CRUD方法
 *
 * @author S.PKAQ
 */
@Getter
public abstract class Ctrl {
    @Autowired
    private I18NHelper i18NHelper;

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
        return new Response<T>().success(null, BizCodeEnum.OPERATE_SUCCESS);
    }

//    protected Response exclude(Object data, String... values){
//        Response response = new Response<>();
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
        return new Response<T>().success(data, BizCodeEnum.OPERATE_SUCCESS);
    }

    /**
     * 返回成功结果
     *
     * @param data
     * @return
     */
    protected <T> Response<T> success(T data, String msg) {
        return new Response<T>().success(msg, data);
    }

    /**
     * 返回成功结果
     *
     * @param data
     * @param bizCode
     * @return
     */
    protected <T> Response<T> success(T data, BizCode bizCode) {
        return new Response<T>().success(bizCode.getMsg(),data);
    }

    /**
     * 返回失败结果
     *
     * @param msg
     * @return
     */
    protected Response<Object> failure(String msg) {
        return new Response<>().failure(msg);
    }

    /**
     * 返回失败结果
     *
     * @param failCode
     * @return
     */

    protected Response<Object> failure(BizCode failCode) {
        return new Response<>().failure(failCode);
    }

    /**
     * 返回失败结果
     *
     * @param failCode
     * @param args
     * @return
     */

    protected Response<Object> failure(BizCode failCode, Object... args) {
        return new Response<>().failure(failCode, args);
    }

    /**
     * 返回失败结果
     *
     * @param failCode
     * @return
     */
    protected Response<Object> failure(String failCode, String msg) {
        return new Response<>().failure(failCode, msg);
    }

    /**
     * 返回失败结果
     *
     * @return
     */
    protected Response<Object> failure() {
        return new Response<>().failure(BizCodeEnum.SERVER_ERROR);
    }
}
