package org.pkaq.core.log.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.regex.Pattern;

/**
 * 日志敏感数据脱敏与长度控制工具。
 *
 * @author PKAQ
 */
public final class LogSanitizer {
    private static final String MASK = "******";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private static final int MAX_JSON_DEPTH = 32;
    private static final Pattern SECRET_FIELD_PATTERN = Pattern.compile(
            "(?i)(?:password|passwd|pwd|token|authorization|cookie|secret|api[-_]?key)"
    );
    private static final Pattern QUERY_SECRET_PATTERN = Pattern.compile(
            "(?i)([?&;](?:password|passwd|pwd|token|authorization|cookie|secret|api[-_]?key)=)[^&#;\\s]*");
    private static final Pattern AUTHORIZATION_HEADER_PATTERN = Pattern.compile(
            "(?i)(\\bauthorization\\s*:\\s*)(?:bearer\\s+)?[^\\s,;]+");

    private LogSanitizer() {
    }

    /**
     * 判断日志参数路径是否指向凭据字段。
     *
     * @param fieldName 字段名或参数路径
     * @return 是否需要隐藏字段值
     */
    public static boolean isSecretField(String fieldName) {
        return fieldName != null && SECRET_FIELD_PATTERN.matcher(fieldName).find();
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
        String sanitized = sanitizeContent(value, 0);
        if (sanitized.length() <= maxLength) {
            return sanitized;
        }
        return sanitized.substring(0, maxLength) + "...[TRUNCATED]";
    }

    private static String sanitizeContent(String value, int depth) {
        String trimmed = value.stripLeading();
        if (trimmed.startsWith("{") || trimmed.startsWith("[") || trimmed.startsWith("\"")) {
            try {
                return sanitizeJson(JSON_MAPPER.readTree(value), depth).toString();
            } catch (JsonProcessingException ignored) {
                // 无法完整解析的结构化文本整体隐藏，不尝试用正则猜测凭据值的边界。
            }
        }
        if (value.indexOf('{') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\\') >= 0
                || value.indexOf('[') >= 0 && isSecretField(value)) {
            return MASK;
        }
        String sanitized = QUERY_SECRET_PATTERN.matcher(value).replaceAll("$1" + MASK);
        return AUTHORIZATION_HEADER_PATTERN.matcher(sanitized).replaceAll("$1" + MASK);
    }

    private static JsonNode sanitizeJson(JsonNode node, int depth) {
        if (depth >= MAX_JSON_DEPTH) {
            return TextNode.valueOf(MASK);
        }
        if (node instanceof ObjectNode object) {
            object.properties().forEach(entry -> object.set(entry.getKey(), isSecretField(entry.getKey())
                    ? TextNode.valueOf(MASK) : sanitizeJson(entry.getValue(), depth + 1)));
        } else if (node instanceof ArrayNode array) {
            for (int index = 0; index < array.size(); index++) {
                array.set(index, sanitizeJson(array.get(index), depth + 1));
            }
        } else if (node.isTextual()) {
            return TextNode.valueOf(sanitizeContent(node.textValue(), depth + 1));
        }
        return node;
    }
}
