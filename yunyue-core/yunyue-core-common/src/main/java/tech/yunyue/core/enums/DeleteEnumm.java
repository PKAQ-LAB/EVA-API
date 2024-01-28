package tech.yunyue.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 逻辑删除
 */
@Getter
@AllArgsConstructor
public enum DeleteEnumm {
    /**
     * 删除
     */
    DELETE("删除", LocalDateTime.now()),
    NOT_DELETE("未删除", null);

    /**
     * 名称
     */
    private String msg;
    /**
     * 索引
     */
    private LocalDateTime code;
}
