package io.nerv.exception

import io.nerv.core.enums.BizCode
import lombok.Getter
import org.springframework.security.core.AuthenticationException

/**
 * 自定义鉴权异常类
 */
@Getter
class OathException : AuthenticationException {
    // 错误码
    var code: String? = null

    // 消息内容
    var msg: String? = null

    constructor(msg: String?) : super(msg) {}

    constructor(errorCodeEnum: BizCode) : super(errorCodeEnum.getName()) {
        code = errorCodeEnum.getIndex()
        msg = errorCodeEnum.getName()
    }
}