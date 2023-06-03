package tech.yunyue.sys.post.errorcode;

import tech.yunyue.core.exception.BizAssert;

/**
 * @author dmz
 */
public enum SYSCode implements BizAssert {

    MISS_CODE_OR_NAME("未接收到岗位管理编码或名称", "0x001-0701-0001"),
    DUPLICATE_CODE_OR_NAME("岗位管理编码或名称已存在", "0x001-0701-0002"),
    DELETE_LIMIT("超过最大限制,最多仅允许同时删除100条记录", "0x001-0701-0003"),

    RECORD_NOT_FOUND("记录未找到或已删除", "0x001-0701-0004");

    private String msg;
    private String code;

    public String getMsg() {
        return this.msg;
    }

    public String getCode() {
        return this.code;
    }

    private SYSCode(String msg, String code) {
        this.msg = msg;
        this.code = code;
    }
}
