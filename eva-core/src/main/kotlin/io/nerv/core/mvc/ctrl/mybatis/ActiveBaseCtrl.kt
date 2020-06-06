package io.nerv.core.mvc.ctrl.mybatis

import cn.hutool.core.collection.CollectionUtil
import com.baomidou.mybatisplus.extension.activerecord.Model
import io.nerv.core.enums.BizCode
import io.nerv.core.enums.BizCodeEnum
import io.nerv.core.enums.ResponseEnumm
import io.nerv.core.mvc.service.mybatis.ActiveBaseService
import io.nerv.core.mvc.util.Response
import io.nerv.core.mvc.util.SingleArray
import io.nerv.core.util.I18NHelper
import io.nerv.exception.ParamException
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import lombok.Getter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

/**
 * Controller 基类
 * Datetime: 2018/3/5 23:37
 * @author S.PKAQ
 */
@Getter
abstract class ActiveBaseCtrl<T : ActiveBaseService<*, *>?, E : Model<*>?> {
    @Autowired
    protected var service: T? = null

    @Autowired
    protected var i18NHelper: I18NHelper? = null

    @PostMapping("del")
    @ApiOperation(value = "根据ID删除/批量删除记录", response = Response::class)
    fun del(@ApiParam(name = "ids", value = "[记录ID]") @RequestBody ids: SingleArray<String?>?): Response? {
        if (null == ids || CollectionUtil.isEmpty(ids.param)) {
            throw ParamException(locale("param_id_notnull"))
        }
        service!!.delete(ids.param)
        return success(service!!.listPage(null, 1), ResponseEnumm.DELETE_SUCCESS.getName())
    }

    @PostMapping("edit")
    @ApiOperation(value = "新增/编辑记录", response = Response::class)
    fun save(@ApiParam(name = "formdata", value = "模型对象") @RequestBody entity: E): Response? {
        service!!.merge(entity)
        return success(entity, ResponseEnumm.SAVE_SUCCESS.getName())
    }

    @GetMapping("list")
    @ApiOperation(value = "列表查询", response = Response::class)
    fun list(@ApiParam(name = "condition", value = "模型对象") entity: E, pageNo: Int?, pageSize: Int?): Response? {
        return this.success(service!!.listPage(entity, pageNo, pageSize))
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "根据ID获得记录信息", response = Response::class)
    operator fun get(@ApiParam(name = "id", value = "记录ID") @PathVariable("id") id: String?): Response? {
        return this.success(service!!.getById(id))
    }

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
     * 返回失败结果
     * @return
     */
    protected fun failure(): Response? {
        return Response().failure(BizCodeEnum.SERVER_ERROR)
    }

    /**
     * 返回失败结果
     * @return
     */
    protected fun failure(bizCode: BizCode): Response? {
        return Response().failure(bizCode)
    }
}