package io.nerv.core.enums;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/4/16 21:49
 */
public enum ResponseEnumm {
    /**
     * 可用
     */
    OPERATE_SUCCESS("操作成功"),
    SAVE_SUCCESS("保存成功"),
    EDIT_SUCCESS("编辑成功"),
    DELETE_SUCCESS("删除成功"),
    QUERY_SUCCESS("查询成功"),
    NULL_MSG(null),
    OPERATE_FAILED("操作失败");

    /**
     * 名称
     */
    private String name;

    ResponseEnumm(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
