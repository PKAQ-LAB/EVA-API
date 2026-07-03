package org.pkaq.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author PKAQ
 */
@Getter
@AllArgsConstructor
public enum FrozenEnumm implements BaseEnum<Integer> {
    // 已锁定
    FROZEN(1),
    // 正常
    UN_FROZEN(0),
    // 只读
    READ_ONLY(9999);

    @JsonValue
    private final Integer code;

    FrozenEnumm(int code) {
        this.code = code;
    }
}
