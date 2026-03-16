package org.pkaq.core.auth;

import lombok.Getter;
import org.pkaq.core.exception.BizAssert;

/**
 * @author PKAQ
 */
@Getter
public enum AuthCodes implements BizAssert {
    LOGIN_FAILED("登录失败", "0x000-00000"),
    ACCOUNT_OR_PWD_ERROR("用户名或密码错误", "0x000-00001"),
    ACCOUNT_LOCKED("用户已经被锁定", "0x000-00003"),
    LOGIN_ERROR("登录遇到未知错误: [{0}]", "0x000-00004"),
    LOGIN_EXPIRED("未登录或登录已失效, 请重新登录", "0x000-00005"),
    PERMISSION_EXPIRED("用户权限不足，请联系管理员", "0x000-00006"),
    LOGIN_FAIL_COUNT_LOCKED("登录验证失败次数过多，请[{0}]分钟后再试", "0x000-00007"),
    LOGIN_CAPTCHA_FAIL("验证码失败，请重试", "0x000-00008"),
    LOGIN_REPLACED("您的账号在另一个地点登录, 您已被迫下线", "0x000-00011"),
    LOGIN_TENANT_AUTH_EXPIRED("当前授权已失效", "0x000-00012"),

    OPENAPI_INVALID_TIMESTAMP_FORMAT("无效的时间戳格式", "0x000-00013"),
    OPENAPI_APP_KEY_NOT_FOUND("AppKey未找到", "0x000-00014"),
    OPENAPI_INVALID_OR_EXPIRED_APP_KEY("无效或过期的AppKey", "0x000-00015"),
    OPENAPI_INVALID_SIGNATURE("无效的签名", "0x000-00016"),
    OPENAPI_NO_PERMISSION("无权限访问此API", "0x000-00017"),
    OPENAPI_AUTHENTICATION_FAILED("认证失败", "0x000-00018"),
    OPENAPI_UNEXPECTED_AUTH_ERROR("认证异常错误", "0x000-00019"),
    OPENAPI_TIMESTAMP_OUT_OF_TOLERANCE("时间戳超出容忍范围", "0x000-00020"),
    OPENAPI_SIGNATURE_GENERATION_FAILED("生成签名失败", "0x000-00021"),
    OPENAPI_REQUEST_ERROR("请求资源无法访问", "0x000-00022");

    @Getter
    private final String msg;
    private final String code;
    private final String prefix = "auth";

    AuthCodes(String msg, String code) {
        this.msg = msg;
        this.code = code;
    }
}

