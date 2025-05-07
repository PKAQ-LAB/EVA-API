package org.pkaq.core.mybatis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pkaq.core.enums.BizCode;

/**
 * @author: S.PKAQ
 */
@Getter
@AllArgsConstructor
public enum FrozenEnumm {
    // 已冻结
    FROZEN("0001"),
    // 未冻结
    UN_FROZEN("0000");

    private final String code;
}
