package org.pkaq.core.bizlog.base;

/**
 * 业务日志类型
 * @author: S.PKAQ
 * @Datetime: 2018/9/26 21:33
 */
public enum BizLogEnum {
    /** 新增 **/
    CREATE("增加", "C"),
    /** 删除 **/
    DELETE("删除", "D"),
    /** 更新 **/
    UPDATE("更新", "U"),
    /** 查询 **/
    QUERY("查询", "R");

    private String name;
    private String index;

    BizLogEnum(String name, String index) {
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
