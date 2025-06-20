package org.pkaq.core.log.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pkaq.core.codes.BizCode;

/**
 * 业务日志类型
 *
 * @author: S.PKAQ
 */
@Getter
@AllArgsConstructor
public enum BizLogEnum implements BizCode {
    /**
     * 新增
     **/
    CREATE("增加", "C"),
    /**
     * 删除
     **/
    DELETE("删除", "D"),
    /**
     * 更新
     **/
    UPDATE("更新", "U"),
    /**
     * 查询
     **/
    QUERY("查询", "R"),

    /**
     * 新增或更新操作<br/>
     **/
    EDIT("编辑", "AOE"),
    /**
     * 导入
     */
    IMPORT("导入", "I"),
    /**
     * 导出
     */
    EXPORT("导出", "E");

    /**
     * 名称
     */
    private final String msg;
    /**
     * 索引
     */
    private final String code;
}
