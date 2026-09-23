package org.pkaq.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author PKAQ
 */
@Getter
@AllArgsConstructor
public enum DelEnumm implements BaseEnum<Integer> {
    // 未删除
    UN_DELETED(0);

    @JsonValue
    private final Integer code;

    DelEnumm(int code) {
        this.code = code;
    }
}
