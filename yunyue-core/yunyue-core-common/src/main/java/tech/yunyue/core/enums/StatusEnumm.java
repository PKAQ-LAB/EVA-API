package tech.yunyue.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: S.PKAQ
 */
@Getter
@AllArgsConstructor
public enum StatusEnumm implements BizCode {
    /**
     * 可用
     */
    ENABLE("正常", "0000"),
    UNABLE("不可用", "0001");

    /**
     * 名称
     */
    private String msg;
    /**
     * 索引
     */
    private String code;
}
