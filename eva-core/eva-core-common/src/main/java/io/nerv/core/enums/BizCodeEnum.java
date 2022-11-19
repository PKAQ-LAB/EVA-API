package io.nerv.core.enums;

import io.nerv.core.exception.BizAssert;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态码
 * 错误统一为 4xxx
 * @author PKAQ
 */
@Getter
@AllArgsConstructor
public enum BizCodeEnum implements BizAssert {

    /** 请求成功 000x **/
    OPERATE_SUCCESS("操作成功", "0000"),
    LOGIN_SUCCESS("登录成功，欢迎回来。", "0001"),
    SAVE_SUCCESS("数据保存成功", "0002"),
    LOGIN_SUCCESS_WELCOME("登录成功，欢迎回来 [{0}]", "0003"),
    LOGINOUT_SUCCESS("已经成功退出登录", "0005"),

    LICENSE_INSTALLED("License 安装成功", "0010"),
    LICENSE_VERIFIED("License 验证成功", "0011"),
    LICENSE_REFRESH("License 更新成功", "0012"),
    LICENSE_IPMAC_VERIFIED("IP、MAC地址验证通过", "0013"),

    /** 请求失败 400x **/
    TOKEN_NOT_VERIFY("Token校验异常","4000"),
    PARAM_ERROR("请求参数错误.", "4006"),
    PARAM_LOST("请求参数丢失.", "4007"),
    PARAM_TYPEERROR("参数类型错误 ", "4008"),
    PARAM_LENGTH("参数长度错误", "4009"),
    SERVER_ERROR("服务器发生错误,请联系管理员.", "4010"),
    REQUEST_METHOD_ERROR("服务器不支持当前请求的方法.", "4011"),
    REQUEST_TOO_MORE("当前请求过于频繁，请稍后再试.", "4012"),
    REQUEST_MEDIA_ERROR("服务器不支持当前请求的类型.", "4015"),

    /** 用户相关 410x **/
    LOGIN_FAILED("登录失败", "4100"),
    ACCOUNT_NOT_EXIST("登录用户不存在", "4101"),
    ACCOUNT_OR_PWD_ERROR("用户名或密码错误", "4102"),
    ACCOUNT_LOCKED("用户已经被锁定", "4103"),
    LOGIN_ERROR("登录遇到未知错误: [{0}]", "4104"),
    LOGIN_EXPIRED("您的登录已失效, 请重新登录.", "4105"),
    PERMISSION_EXPIRED("用户权限不足，请联系管理员", "4106"),
    ACCOUNT_ALREADY_EXIST("账号名已存在", "4110"),

    /** 权限相关 420x **/
    PERMISSION_DENY ("权限不足", "4200"),

    /** license相关 421x **/
    LICENSE_BADIP ("IP 地址验证不通过", "4210"),
    LICENSE_BADMAC ("MAC 地址验证不通过", "4211"),
    LICENSE_LICENSEHASEXPIRED ("证书已经过期", "4213"),
    LICENSE_LICENSEVERIFYFAILED ("证书验证失败", "4214"),

    /** 业务 430x ~ 439x **/
    PATH_ALREADY_EXIST("模块路径已经存在", "4301"),
    CHILD_EXIST("[{0}] 存在子节点，无法删除。", "4302"),
    CODE_EXIST("[{0}] 编码已经存在", "4303"),
    ROLE_CODE_EXIST("权限编码已经存在", "4304"),
    RESOURCE_USED("资源已经被引用，无法删除。", "4305"),
    PARENT_NOT_AVAILABLE("父节点为禁用状态，无法启用。", "4306"),
    MODULE_RESOURCE_USED("模块下存在已经被引用的资源，无法删除。", "4307"),
    BAD_ORG_PASSWORD("原始密码校验失败", "4308"),
    ORG_CODE_EXIST("组织(部门)编码已经存在", "4310"),

    NULL_ID("所需记录ID为空值", "4311"),
    NULL_PARAM_ID("所需记录 [ {0} ] 为空值", "4312"),

    /** 上传相关 430x ~ 432x **/
    FILEIO_ERROR("文件读取时发生错误", "4306"),
    FILENAME_ERROR("文件名称错误", "4307"),
    FILESAVE_ERROR("文件保存失败", "4308"),
    FILETYPE_NOT_SUPPORTED("不支持的文件格式", "4309"),

    /** 三方调用 **/
    /** 微信 440x ~ 442x **/
    WEIXIN_JSTICKET_ERROR("获取jsapi_ticket失败", "4401"),
    WEIXIN_PAYSIGN_ERROR("支付回调验签失败", "4402");

    /**
     * 名称
     */
    private String msg;
    /**
     * 索引
     */
    private String code;
}
