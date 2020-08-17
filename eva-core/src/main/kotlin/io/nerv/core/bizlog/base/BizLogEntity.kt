package io.nerv.core.bizlog.base

import lombok.experimental.Accessors

/**
 * 业务日志模型类
 * @author: S.PKAQ
 * @Datetime: 2018/9/26 21:28
 */
@Accessors(chain = true)
open class BizLogEntity {
    /** 操作人  */
    var operator: String? = null

    /** 操作类型  */
    var operateType: String? = null

    /** 操作时间  */
    var operateDatetime: String? = null

    /** 操作描述  */
    var description: String? = null

    /** 类名  */
    var className: String? = null

    /** 方法名  */
    var method: String? = null

    /** 参数  */
    var params: String? = null

    /** 返回结果  */
    var response: String? = null

    /** 设备类型  */
    var device: String? = null

    /** 应用版本  */
    var version: String? = null
}