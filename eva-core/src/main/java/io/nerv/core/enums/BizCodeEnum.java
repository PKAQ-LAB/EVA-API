package io.nerv.core.enums;

import lombok.Getter;

/**
 * 状态码
 * 错误统一为 4xxx
 * @author PKAQ
 */
@Getter
public enum BizCodeEnum {
    /** 请求成功 **/
    REQUEST_SUCCESS("请求成功", 0000),
    SAVE_SUCCESS("数据保存成功", 0001),
    /** 请求失败 **/
    PARAM_ERROR("请求参数错误.", 4000),
    PARAM_LOST("请求参数丢失.", 4001),
    PARAM_TYPEERROR("参数类型错误 ", 4002),
    PARAM_LENGTH("参数长度错误", 4003),
    SERVER_ERROR("服务器发生错误,请联系管理员.", 4004),
    /** 用户相关 **/
    LOGIN_FAILED("登录失败", 4100),
    ACCOUNT_NOT_EXIST("登录用户不存在", 4101),
    ACCOUNT_OR_PWD_ERROR("用户名或密码错误", 4102),
    ACCOUNT_LOCKED("用户已经被锁定", 4103),
    LOGIN_ERROR("登录遇到未知错误", 4104),
    ACCOUNT_ALREADY_EXIST("账号名已存在", 4110),
    /** 业务 **/
    PATH_ALREADY_EXIST("模块路径已经存在", 4301),
    CHILD_EXIST("[{}] 存在子节点，无法删除。", 4302),
    ROLE_CODE_EXIST("权限编码已经存在", 4303),
    RESOURCE_USED("资源已经被引用，无法删除。", 4304),
    PARENT_NOT_AVAILABLE("父节点为禁用状态，无法启用。", 4305),
    MODULE_RESOURCE_USED("模块下存在已经被引用的资源，无法删除。", 4306),
    /** 上传相关 **/
    FILENAME_ERROR("上传名称错误",4307),
    FILESAVE_ERROR("文件保存失败",4308),
    FILETYPE_NOT_SUPPORTED("不支持的文件格式",4309),

    /** 权限相关 **/
    PERMISSION_DENY ("权限不足", 4200);

    /**
     * 名称
     */
    private String name;
    /**
     * 索引
     */
    private int index;

    BizCodeEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public static String getName(int index) {
        for (BizCodeEnum p : BizCodeEnum.values()) {
            if ( index == p.getIndex() ) {
                return p.name;
            }
        }
        return "-";
    }
}
