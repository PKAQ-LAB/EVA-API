package org.pkaq.core.enums;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/4/16 21:49
 */
public enum StatusEnumm {
    /**
     * 可用
     */
    ENABLE("正常", "0001"),
    UNABLE("不可用", "0000");

    /**
     * 名称
     */
    private String name;
    /**
     * 索引
     */
    private String index;

    StatusEnumm(String name, String index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
