package tech.yunyue.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 逻辑删除
 */
@Getter
@AllArgsConstructor
public enum DeleteEnumm implements BizCode {
    /**
     * 删除
     */
    DELETE("删除", "0001"),
    NOT_DELETE("未删除", "0000");

    /**
     * 名称
     */
    private String msg;
    /**
     * 索引
     */
    private String code;
}
