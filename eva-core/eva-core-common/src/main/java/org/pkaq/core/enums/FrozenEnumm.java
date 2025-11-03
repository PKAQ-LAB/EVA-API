package org.pkaq.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: S.PKAQ
 */
@Getter
@AllArgsConstructor
public enum FrozenEnumm implements BaseEnum<Integer> {
    // 已冻结
    FROZEN(1),
    // 未冻结
    UN_FROZEN(0),
    // 只读
    READ_ONLY(-1);

    @JsonValue
    private final Integer code;

    FrozenEnumm(int code) {
        this.code = code;
    }
}
