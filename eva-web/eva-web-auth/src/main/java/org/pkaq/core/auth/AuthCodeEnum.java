package org.pkaq.core.auth;

import lombok.Getter;
import org.pkaq.core.exception.BizAssert;

/**
 * @author PKAQ
 */
@Getter
public enum AuthCodeEnum implements BizAssert {
    LOGIN_FAILED("登录失败", "0x000-00000"),
    ACCOUNT_NOT_EXIST("用户名或密码错误", "0x000-00001"),
    ACCOUNT_OR_PWD_ERROR("用户名或密码错误", "0x000-00002"),
    ACCOUNT_LOCKED("用户已经被锁定", "0x000-00003"),
    LOGIN_ERROR("登录遇到未知错误: [{0}]", "0x000-00004"),
    LOGIN_EXPIRED("未登录或登录已失效, 请重新登录", "0x000-00005"),
    PERMISSION_EXPIRED("用户权限不足，请联系管理员", "0x000-00006"),
    ACCOUNT_ALREADY_EXIST("账户名已存在", "0x000-00010"),
    LOGIN_FAIL_COUNT_LOCKED("登录验证失败次数过多，请[{0}]分钟后再试", "0x000-00007"),
    LOGIN_CAPTCHA_FAIL("验证失败，请重试", "0x000-00008"),
    LOGIN_REPLACED("您的账号在另一地点登录, 您已被迫下线", "0x000-00011"),
    LOGIN_TENANT_AUTH_EXPIRED("当前授权已失效", "0x000-00012");

    @Getter
    private final String msg;
    private final String code;
    private final String prefix = "auth";

    AuthCodeEnum(String msg, String code) {
        this.msg = msg;
        this.code = code;
    }
}
