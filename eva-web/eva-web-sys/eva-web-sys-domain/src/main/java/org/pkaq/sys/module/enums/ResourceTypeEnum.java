package org.pkaq.sys.module.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 模块资源类型。
 *
 * @author PKAQ
 */
@Getter
@RequiredArgsConstructor
public enum ResourceTypeEnum {

    ALL("*"),
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH"),
    BUTTON("BUTTON"),
    OPERATE("OPERATE");

    private final String code;

    public static boolean exists(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(item -> item.code.equalsIgnoreCase(code.trim()));
    }

    public static String normalize(String code) {
        if (code == null || code.isBlank()) {
            return ALL.code;
        }
        String value = code.trim().toUpperCase();
        return exists(value) ? value : code.trim();
    }
}
