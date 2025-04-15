package org.pkaq.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 逻辑删除
 */
@Getter
@AllArgsConstructor
public enum DeleteEnumm {
    /**
     * 删除
     */
    DELETE("删除", System.currentTimeMillis()),
    NOT_DELETE("未删除", 0);

    /**
     * 名称
     */
    private String msg;
    /**
     * 索引
     */
    private long code;
}
