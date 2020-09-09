package io.nerv.ennm;

import lombok.Getter;

/**
 * 状态码
 * 错误统一为 4xxx
 * @author PKAQ
 */
@Getter
public enum AuthCodeEnum {
    /** 用户相关 410x **/
    LOGIN_FAILED("登录失败", "4100"),
    ACCOUNT_NOT_EXIST("登录用户不存在", "4101"),
    ACCOUNT_OR_PWD_ERROR("用户名或密码错误", "4102"),
    ACCOUNT_LOCKED("用户已经被锁定", "4103"),
    LOGIN_ERROR("登录遇到未知错误", "4104"),
    LOGIN_EXPIRED("您的登录已失效, 请重新登录.", "4105"),
    PERMISSION_EXPIRED("用户权限不足，请联系管理员", "4106"),
    ACCOUNT_ALREADY_EXIST("账号名已存在", "4110"),

    /** 权限相关 420x **/
    PERMISSION_DENY ("权限不足", "4200");

    /**
     * 名称
     */
    private String name;
    /**
     * 索引
     */
    private String index;

    AuthCodeEnum(String name, String index) {
        this.name = name;
        this.index = index;
    }

    public static String getName(String index) {
        for (AuthCodeEnum p : AuthCodeEnum.values()) {
            if ( index.equals(p.getIndex())) {
                return p.name;
            }
        }
        return "-";
    }
}