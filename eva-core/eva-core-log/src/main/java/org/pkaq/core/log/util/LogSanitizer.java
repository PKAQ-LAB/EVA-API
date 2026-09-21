package org.pkaq.core.log.util;

import java.util.regex.Pattern;

/**
 * 日志敏感数据脱敏与长度控制工具。
 *
 * @author PKAQ
 */
public final class LogSanitizer {
    private static final String MASK = "******";
    private static final Pattern JSON_SECRET_PATTERN = Pattern.compile(
            "(?i)(\\\"[^\\\"]*(?:password|passwd|pwd|token|authorization|cookie|secret|api[-_]?key)[^\\\"]*\\\"\\s*:\\s*)"
                    + "(\\\"(?:\\\\.|[^\\\"])*\\\"|[^,}\\s]+)");
    private static final Pattern QUERY_SECRET_PATTERN = Pattern.compile(
            "(?i)([?&;](?:password|passwd|pwd|token|authorization|cookie|secret|api[-_]?key)=)[^&#;\\s]*");
    private static final Pattern AUTHORIZATION_HEADER_PATTERN = Pattern.compile(
            "(?i)(\\bauthorization\\s*:\\s*)(?:bearer\\s+)?[^\\s,;]+");

    private LogSanitizer() {
    }

    /**
     * 对 JSON 文本中的常见凭据字段脱敏并限制长度。
     *
     * @param value 原始文本
     * @param maxLength 最大长度
     * @return 安全日志文本
     */
    public static String sanitize(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String sanitized = JSON_SECRET_PATTERN.matcher(value).replaceAll("$1\"" + MASK + "\"");
        sanitized = QUERY_SECRET_PATTERN.matcher(sanitized).replaceAll("$1" + MASK);
        sanitized = AUTHORIZATION_HEADER_PATTERN.matcher(sanitized).replaceAll("$1" + MASK);
        if (sanitized.length() <= maxLength) {
            return sanitized;
        }
        return sanitized.substring(0, maxLength) + "...[TRUNCATED]";
    }
}
