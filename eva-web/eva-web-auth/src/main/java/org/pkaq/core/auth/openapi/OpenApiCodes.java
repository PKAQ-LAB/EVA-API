package org.pkaq.core.auth.openapi;

import lombok.Getter;
import org.pkaq.core.exception.BizAssert;

/**
 * OpenAPI认证错误码
 *
 * @author PKAQ
 */
@Getter
public enum OpenApiCodes implements BizAssert {
    INVALID_TIMESTAMP_FORMAT("无效的时间戳格式", "0x617069687562-O-0001"),
    APP_KEY_NOT_FOUND("AppKey未找到", "0x617069687562-O-0002"),
    INVALID_OR_EXPIRED_APP_KEY("无效或过期的AppKey", "0x617069687562-O-0003"),
    INVALID_SIGNATURE("无效的签名", "0x617069687562-O-0004"),
    NO_PERMISSION("无权限访问此API", "0x617069687562-O-0005"),
    AUTHENTICATION_FAILED("认证失败", "0x617069687562-O-0006"),
    UNEXPECTED_AUTH_ERROR("认证异常错误", "0x617069687562-O-0007"),
    TIMESTAMP_OUT_OF_TOLERANCE("时间戳超出容忍范围", "0x617069687562-O-0008"),
    SIGNATURE_GENERATION_FAILED("生成签名失败", "0x617069687562-O-0009"),
    REQUEST_ERROR("请求资源无法访问", "0x617069687562-O-0010");

    private final String msg;
    private final String code;
    private final String prefix = "openapi";

    OpenApiCodes(String msg, String code) {
        this.msg = msg;
        this.code = code;
    }
}
