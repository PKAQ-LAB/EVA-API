package org.pkaq.asserts;


import org.pkaq.core.exception.BizAssert;

/**
 * @author dmz
 */
public enum SysCode implements BizAssert {

    MISS_CODE_OR_NAME("未接收到岗位管理编码或名称", "0x001-0701-0001"),
    DUPLICATE_CODE_OR_NAME("岗位管理编码或名称已存在", "0x001-0701-0002"),
    DELETE_LIMIT("超过最大限制,最多仅允许同时删除100条记录", "0x001-0701-0003"),
    RECORD_NOT_FOUND("记录未找到或已删除", "0x001-0701-0004"),
    DELETE_EXISTENCE_CHILD_NODE("删除记录中存在子节点,请检查数据", "0x001-0701-0005"),
    ACCOUNT_ALREADY_EXIST("账号名已存在", "0x001-0701-0007"),
    EMAIL_ALREADY_EXIST("邮箱已存在", "0x001-0701-0008"),
    TEL_ALREADY_EXIST("手机号已存在", "0x001-0701-0009"),
    UNABLE_TO_DELETE("该部门下已存在多个用户，无法删除", "0x001-0201-0002"),
    MODULE_RESOURCE_COED_EXIST("资源code已存在", "0x001-0701-0010"),
    TENANT_COED_NAME_EXIST("租户code/名称已存在", "0x001-0701-0011"),
    TENANT_AUTH_COUNT_MORE("用户超出授权数量", "0x001-0701-0012"),
    ;

    private String msg;
    private String code;

    public String getMsg() {
        return this.msg;
    }

    public String getCode() {
        return this.code;
    }

    private SysCode(String msg, String code) {
        this.msg = msg;
        this.code = code;
    }
}
