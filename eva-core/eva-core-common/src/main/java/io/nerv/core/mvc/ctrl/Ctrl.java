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
    protected Response success() {
        return new Response<>().success(null);
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
    protected Response success(Object data) {
        return new Response<>().success(data);
    }

    /**
     * 返回成功结果
     *
     * @param data
     * @return
     */
    protected Response success(Object data, String msg) {
        return new Response<>().success(data, msg);
    }

    /**
     * 返回成功结果
     *
     * @param data
     * @param bizCode
     * @return
     */
    protected Response success(Object data, BizCode bizCode) {
        return new Response<>().success(data, bizCode.getMsg());
    }

    /**
     * 返回失败结果
     *
     * @param failCode
     * @return
     */
    protected Response failure(BizCode failCode) {
        return new Response<>().failure(failCode);
    }

    /**
     * 返回失败结果
     *
     * @param failCode
     * @param args
     * @return
     */
    protected Response failure(BizCode failCode, Object... args) {
        return new Response<>().failure(failCode, args);
    }

    /**
     * 返回失败结果
     *
     * @param failCode
     * @return
     */
    protected Response failure(String failCode, String msg) {
        return new Response<>().failure(failCode, msg);
    }

    /**
     * 返回失败结果
     *
     * @return
     */
    protected Response failure() {
        return new Response<>().failure(BizCodeEnum.SERVER_ERROR);
    }
}
