package org.pkaq.core.mybatis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: S.PKAQ
 */
@Getter
@AllArgsConstructor
public enum FrozenEnumm {
    // 已冻结
    FROZEN(1),
    // 未冻结
    UN_FROZEN(0);

    private final int code;
}
