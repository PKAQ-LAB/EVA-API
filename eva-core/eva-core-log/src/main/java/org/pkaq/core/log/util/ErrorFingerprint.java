package org.pkaq.core.log.util;

import org.pkaq.core.advice.ExceptionInfo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

/**
 * 错误事件指纹计算工具。
 *
 * @author PKAQ
 */
public final class ErrorFingerprint {
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "(?i)\\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\b");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+\\b");

    private ErrorFingerprint() {
    }

    /**
     * 基于异常类型、代码位置和归一化摘要计算 SHA-256 指纹。
     *
     * @param info 异常元数据
     * @return 稳定的错误指纹
     */
    public static String calculate(ExceptionInfo info) {
        String source = String.join("|",
                safe(info.getExceptionType()),
                safe(info.getClassName()),
                safe(info.getMethod()),
                normalize(info.getSummary()));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(source.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JDK 缺少 SHA-256 算法", exception);
        }
    }

    private static String normalize(String value) {
        String normalized = UUID_PATTERN.matcher(safe(value)).replaceAll("{uuid}");
        return NUMBER_PATTERN.matcher(normalized).replaceAll("{number}");
    }

    private static String safe(String value) {
        return value == null ? "" : value.strip();
    }
}
