package tech.yunyue.core.log.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tech.yunyue.core.enums.BizCode;

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
     * 与{@link tech.yunyue.core.log.annotation.BizLog#distinguishParam}搭配使用
     **/
    CREATE_UPDATE("新增/更新", "CR"),
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
    private String msg;
    /**
     * 索引
     */
    private String code;
}
