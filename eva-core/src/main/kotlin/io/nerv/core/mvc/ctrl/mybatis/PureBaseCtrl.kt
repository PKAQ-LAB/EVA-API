package io.nerv.core.mvc.ctrl.mybatis

import io.nerv.core.enums.BizCode
import io.nerv.core.enums.BizCodeEnum
import io.nerv.core.mvc.service.mybatis.StdBaseService
import io.nerv.core.mvc.util.Response
import io.nerv.core.util.I18NHelper
import lombok.Getter
import org.springframework.beans.factory.annotation.Autowired

/**
 * Controller 基类- 无默认CRUD方法
 * Datetime: 2018/3/5 23:37
 * @author S.PKAQ
 */
@Getter
abstract class PureBaseCtrl<T : StdBaseService<*, *>?> {
    @JvmField
    @Autowired
    protected var service: T? = null

    @Autowired
    protected var i18NHelper: I18NHelper? = null

    /**
     * 根据编码获取国际化字符串
     * @param code
     * @return
     */
    protected fun locale(code: String?): String {
        return i18NHelper!!.getMessage(code)
    }

    /**
     * 操作成功
     * @return
     */
    protected fun success(): Response? {
        return Response().success(null)
    }

    protected fun exclude(data: Any?, values: Array<String>): Response {
        val response = Response()
        response.exclude(data, values)
        return response
    }

    /**
     * 返回成功结果
     * @param data
     * @return
     */
    protected fun success(data: Any?): Response? {
        return Response().success(data)
    }

    /**
     * 返回成功结果
     * @param data
     * @return
     */
    protected fun success(data: Any?, msg: String?): Response? {
        return Response().success(data, msg)
    }

    /**
     * 返回成功结果
     * @param data
     * @param msg
     * @return
     */
    protected fun success(data: Any?, msg: BizCode): Response? {
        return Response().success(data, msg.getName())
    }

    /**
     * 返回失败结果
     * @param failCode
     * @return
     */
    protected fun failure(failCode: BizCode): Response? {
        return Response().failure(failCode)
    }

    /**
     * 返回失败结果
     * @param failCode
     * @return
     */
    protected fun failure(failCode: String?, msg: String?): Response? {
        return Response().failure(failCode, msg)
    }

    /**
     * 返回失败结果
     * @return
     */
    protected fun failure(): Response? {
        return Response().failure(BizCodeEnum.SERVER_ERROR)
    }
}