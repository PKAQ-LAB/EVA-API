package io.nerv.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: S.PKAQ
 */
@Getter
@AllArgsConstructor
public enum StatusEnumm implements BizCode{
    /**
     * 可用
     */
    ENABLE("正常", "0001"),
    UNABLE("不可用", "0000");

    /**
     * 名称
     */
    private String name;
    /**
     * 索引
     */
    private String index;
}
