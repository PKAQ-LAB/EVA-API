package org.pkaq.core.enums;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/4/16 21:49
 */
public enum ResponseEnumm {
    /**
     * 可用
     */
    OPERATE_SUCCESS("操作成功"),
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
