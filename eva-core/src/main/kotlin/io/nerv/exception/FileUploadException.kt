package io.nerv.exception

import io.nerv.core.enums.BizCodeEnum
import lombok.Getter

/**
 * 图片上传异常
 */
@Getter
class FileUploadException : RuntimeException {
    var msg: String

    constructor(msg: String) : super() {
        this.msg = msg
    }

    constructor(bizCodeEnum: BizCodeEnum) : super() {
        msg = bizCodeEnum.code
    }
}