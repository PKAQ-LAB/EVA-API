package org.pkaq.web.sys.user.entity;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/4/10 7:21
 */
public enum UserEnumm {
    LOCK("锁定", "1"),
    UNLOCK("正常", "0");

    /**
     * 名称
     */
    private String name;
    /**
     * 索引
     */
    private String index;

    UserEnumm(String name, String index) {
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
