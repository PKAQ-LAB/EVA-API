package org.pkaq.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: S.PKAQ
 */
@Getter
@AllArgsConstructor
public enum DelEnumm implements BaseEnum<Integer> {
    // 未删除
    UN_DELETED(0);

    DelEnumm(int code) {
        this.code = code;
    }

    @JsonValue
    private final Integer code;
}
