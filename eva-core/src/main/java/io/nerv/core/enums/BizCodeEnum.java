package io.nerv.core.enums;

import lombok.Getter;

/**
 * 状态码
 * 错误统一为 4xxx
 * @author PKAQ
 */
@Getter
public enum BizCodeEnum implements BizCode {
    /** 请求成功 **/
    OPERATE_SUCCESS("操作成功", "0000"),
    LOGIN_SUCCESS("登录成功，欢迎回来。", "0001"),
    LOGIN_SUCCESS_WELCOME("登录成功，欢迎回来 [{}]", "0003"),
    LOGINOUT_SUCCESS("已经成功退出登录", "0005"),
    SAVE_SUCCESS("数据保存成功", "0002"),
    /** 请求失败 **/
    PARAM_ERROR("请求参数错误.", "4000"),
    PARAM_LOST("请求参数丢失.", "4001"),
    PARAM_TYPEERROR("参数类型错误 ", "4002"),
    PARAM_LENGTH("参数长度错误", "4003"),
    SERVER_ERROR("服务器发生错误,请联系管理员.", "4004"),
    REQUEST_METHOD_ERROR("服务器不支持当前请求的方法.", "4005"),
    REQUEST_MEDIA_ERROR("服务器不支持当前请求的类型.", "4015"),
    /** 用户相关 **/
    LOGIN_FAILED("登录失败", "4100"),
    ACCOUNT_NOT_EXIST("登录用户不存在", "4101"),
    ACCOUNT_OR_PWD_ERROR("用户名或密码错误", "4102"),
    ACCOUNT_LOCKED("用户已经被锁定", "4103"),
    LOGIN_ERROR("登录遇到未知错误", "4104"),
    LOGIN_EXPIRED("您的登录已失效,请重新登录", "4105"),
    ACCOUNT_ALREADY_EXIST("账号名已存在", "4110"),
    /** 业务 **/
    PATH_ALREADY_EXIST("模块路径已经存在", "4301"),
    CHILD_EXIST("[{}] 存在子节点，无法删除。", "4302"),
    ROLE_CODE_EXIST("权限编码已经存在", "4303"),
    RESOURCE_USED("资源已经被引用，无法删除。", "4304"),
    PARENT_NOT_AVAILABLE("父节点为禁用状态，无法启用。", "4305"),
    MODULE_RESOURCE_USED("模块下存在已经被引用的资源，无法删除。", "4306"),
    // 部门管理
    ORG_CODE_EXIST("组织(部门)编码已经存在", "4310"),
    /** 上传相关 **/
    FILENAME_ERROR("上传名称错误", "4307"),
    FILESAVE_ERROR("文件保存失败", "4308"),
    FILETYPE_NOT_SUPPORTED("不支持的文件格式", "4309"),
    /** 三方调用 **/
    /** 微信 **/
    WEIXIN_JSTICKET_ERROR("获取jsapi_ticket失败", "4401"),
    WEIXIN_PAYSIGN_ERROR("支付回调验签失败", "4402"),


    /** 权限相关 **/
    PERMISSION_DENY ("权限不足", "4200");


    /**
     * 名称
     */
    private String name;
    /**
     * 索引
     */
    private String index;

    BizCodeEnum(String name, String index) {
        this.name = name;
        this.index = index;
    }

    public static String getName(String index) {
        for (BizCodeEnum p : BizCodeEnum.values()) {
            if ( index.equals(p.getIndex())) {
                return p.name;
            }
        }
        return "-";
    }
}
