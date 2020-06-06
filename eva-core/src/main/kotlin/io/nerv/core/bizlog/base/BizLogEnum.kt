package io.nerv.core.bizlog.base

/**
 * 业务日志类型
 * @author: S.PKAQ
 * @Datetime: 2018/9/26 21:33
 */
enum class BizLogEnum(override var name: String, var index: String) {
    /** 新增  */
    CREATE("增加", "C"),

    /** 删除  */
    DELETE("删除", "D"),

    /** 更新  */
    UPDATE("更新", "U"),

    /** 查询  */
    QUERY("查询", "R");

}