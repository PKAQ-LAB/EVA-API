package io.nerv.exception

import io.nerv.core.enums.BizCode
import lombok.Getter

/**
 * 关于业务的异常
 */
@Getter
class BizException : RuntimeException {
    // 错误码
    var code: String? = null

    // 消息内容
    var msg: String?

    // 业务代码
    var bizCode: BizCode? = null

    constructor(msg: String?) : super(msg) {
        this.msg = msg
    }

    constructor(code: String?, msg: String?) : super(msg) {
        this.code = code
        this.msg = msg
    }

    constructor(bizCode: BizCode) : super(bizCode.getName()) {
        code = bizCode.getIndex()
        msg = bizCode.getName()
        this.bizCode = bizCode
    }
}