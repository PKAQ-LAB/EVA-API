package io.nerv.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: S.PKAQ
 */
@Getter
@AllArgsConstructor
public enum LockEnumm implements BizCode{
    /**
     * 锁定
     */
    LOCK("锁定", "0001"),
    UNLOCK("正常", "0000");

    /**
     * 名称
     */
    private String name;
    /**
     * 索引
     */
    private String index;
}
