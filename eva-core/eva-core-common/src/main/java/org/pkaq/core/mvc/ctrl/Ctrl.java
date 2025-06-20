package org.pkaq.core.mvc.ctrl;

import lombok.Getter;
import org.pkaq.core.codes.BizCode;
import org.pkaq.core.codes.CommonCodes;
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
        return Response.success(null, CommonCodes.OPERATE_SUCCESS);
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
        return Response.success(data, CommonCodes.OPERATE_SUCCESS);
    }

    /**
     * 返回成功结果
     *
     * @param data
     * @return
     */
    protected <T> Response<T> success(T data, String msg) {
        return Response.success(msg, data);
    }

    /**
     * 返回成功结果
     *
     * @param data
     * @param bizCode
     * @return
     */
    protected <T> Response<T> success(T data, BizCode bizCode) {
        return Response.success(bizCode.getMsg(),data);
    }

    /**
     * 返回失败结果
     *
     * @param msg
     * @return
     */
    protected Response<Object> failure(String msg) {
        return Response.failure(msg);
    }

    /**
     * 返回失败结果
     *
     * @param failCode
     * @return
     */

    protected Response<Object> failure(BizCode failCode) {
        return Response.failure(failCode);
    }

    /**
     * 返回失败结果
     *
     * @param failCode
     * @param args
     * @return
     */

    protected Response<Object> failure(BizCode failCode, Object... args) {
        return Response.failure(failCode, args);
    }

    /**
     * 返回失败结果
     *
     * @param failCode
     * @return
     */
    protected Response<Object> failure(String failCode, String msg) {
        return Response.failure(failCode, msg);
    }

    /**
     * 返回失败结果
     *
     * @return
     */
    protected Response<Object> failure() {
        return Response.failure(CommonCodes.SERVER_ERROR);
    }
}
