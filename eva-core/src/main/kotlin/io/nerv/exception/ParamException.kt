package io.nerv.exception

import io.nerv.core.enums.BizCode
import io.nerv.core.enums.BizCodeEnum
import lombok.Getter

/**
 * 参数错误异常
 * Author: S.PKAQ
 * Datetime: 2018/3/6 14:50
 */
class ParamException : RuntimeException {
    var msg: String?

    constructor() : super() {
        msg = BizCodeEnum.PARAM_ERROR.getName()
    }

    constructor(msg: String?) : super() {
        this.msg = msg
    }

    constructor(bizCodeEnum: BizCode) : super() {
        msg = bizCodeEnum.getName()
    }
}