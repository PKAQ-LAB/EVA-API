package io.nerv.core.bizlog.base

import lombok.Data
import lombok.experimental.Accessors

/**
 * 业务日志模型类
 * @author: S.PKAQ
 * @Datetime: 2018/9/26 21:28
 */
@Data
@Accessors(chain = true)
open class BizLogEntity {
    /** 操作人  */
    private val operator: String? = null

    /** 操作类型  */
    private val operateType: String? = null

    /** 操作时间  */
    private val operateDatetime: String? = null

    /** 操作描述  */
    private val description: String? = null

    /** 类名  */
    private val className: String? = null

    /** 方法名  */
    private val method: String? = null

    /** 参数  */
    private val params: String? = null

    /** 返回结果  */
    private val response: String? = null

    /** 设备类型  */
    private val device: String? = null

    /** 应用版本  */
    private val version: String? = null
}