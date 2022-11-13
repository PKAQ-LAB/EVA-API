package io.nerv.common.bizlog.base;

import io.nerv.common.enums.BizCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务日志类型
 * @author: S.PKAQ
 */
@Getter
@AllArgsConstructor
public enum BizLogEnum implements BizCode {
    /** 新增 **/
    CREATE("增加", "C"),
    /** 删除 **/
    DELETE("删除", "D"),
    /** 更新 **/
    UPDATE("更新", "U"),
    /** 查询 **/
    QUERY("查询", "R");

    /**
     * 名称
     */
    private String msg;
    /**
     * 索引
     */
    private String code;
}
