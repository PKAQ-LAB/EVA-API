package org.pkaq.core.log.util;

import org.junit.jupiter.api.Test;
import org.pkaq.core.util.json.JsonUtil;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogSanitizerTest {

    @Test
    void recognizesCredentialLabelsAroundLineTerminators() {
        for (String separator : List.of("\n", "\r", "\r\n", "\u0085", "\u2028", "\u2029", "\u000B", "\f")) {
            for (String label : List.of("password", "passwd", "pwd", "token", "authorization",
                    "cookie", "secret", "apiKey", "api_key", "api-key")) {
                assertTrue(LogSanitizer.isSecretField(separator + "prefix" + label + separator + "suffix"));
                assertTrue(LogSanitizer.isSecretField(label + separator));
            }
        }
        assertFalse(LogSanitizer.isSecretField(null));
        assertFalse(LogSanitizer.isSecretField("ordinary\nfield"));
    }

    @Test
    void masksLinebreakKeysInNestedAndEncodedJson() {
        for (String separator : List.of("\n", "\r", "\r\n", "\u0085", "\u2028", "\u2029", "\u000B", "\f")) {
            Map<String, Object> credentials = Map.of("token" + separator, "synthetic-linebreak",
                    separator + "newPassword", List.of("synthetic-linebreak-array"));
            String source = JsonUtil.toJson(Map.of("id", 32, "nested", credentials,
                    "encoded", JsonUtil.toJson(credentials)));

            String result = LogSanitizer.sanitize(source, 16000);

            assertFalse(result.contains("synthetic-linebreak"));
            assertEquals(32, JsonUtil.toMap(result).get("id"));
        }
    }

    @Test
    void hidesPrefixedCredentialArray() {
        assertEquals("******", LogSanitizer.sanitize(
                "prefix {\"token\":[\"synthetic-first\",\"synthetic-second\"]}", 1000));
    }

    @Test
    void hidesPrefixedEscapedCredentialKey() {
        assertEquals("******", LogSanitizer.sanitize(
                "prefix {\"pass" + "\\u0077ord\":\"synthetic-escaped\"}", 1000));
    }

    @Test
    void hidesMalformedCredentialArray() {
        assertEquals("******", LogSanitizer.sanitize(
                "{\"token\":[\"synthetic-first\",\"synthetic-second\"]", 1000));
    }

    @Test
    void hidesUnparseableStructuredTextInsideValidJson() {
        String source = JsonUtil.toJson(Map.of("remark",
                "prefix {\"token\":[\"synthetic-first\",\"synthetic-second\"]}", "id", 30));

        String result = LogSanitizer.sanitize(source, 1000);

        assertEquals("******", JsonUtil.toMap(result).get("remark"));
        assertEquals(30, JsonUtil.toMap(result).get("id"));
    }

    @Test
    void hidesTrailingOrParserDepthFailureInsteadOfReturningRawText() {
        assertEquals("******", LogSanitizer.sanitize(
                "{\"id\":1} trailing {\"token\":\"synthetic-trailing\"}", 1000));
        String tooDeep = "{\"nested\":".repeat(1100)
                + "{\"token\":\"synthetic-too-deep\"}" + "}".repeat(1100);
        assertEquals("******", LogSanitizer.sanitize(tooDeep, 16000));
    }

    @Test
    void preservesOrdinaryDescriptionsAndDoesNotInventBase64Decoding() {
        assertEquals("更新目标[31]", LogSanitizer.sanitize("更新目标[31]", 1000));
        assertEquals("eyJ0b2tlbiI6Im9wYXF1ZSJ9", LogSanitizer.sanitize("eyJ0b2tlbiI6Im9wYXF1ZSJ9", 1000));
    }

    @Test
    void masksEntireCredentialCollections() {
        String source = JsonUtil.toJson(Map.of("nested", Map.of(
                "refreshToken", List.of("synthetic-first", "synthetic-second")), "id", 25));

        String result = LogSanitizer.sanitize(source, 1000);

        assertFalse(result.contains("synthetic-"), "凭据数组必须整体隐藏");
        assertEquals(25, JsonUtil.toMap(result).get("id"));
    }

    @Test
    void masksCredentialsInsideEncodedJsonWithoutBreakingOuterJson() {
        String payload = JsonUtil.toJson(Map.of("accessToken", "synthetic-encoded"));
        String source = JsonUtil.toJson(Map.of("payload", payload, "id", 26));

        String result = LogSanitizer.sanitize(source, 1000);

        assertFalse(result.contains("synthetic-encoded"), "嵌套JSON凭据不得泄露");
        assertEquals(26, JsonUtil.toMap(result).get("id"));
    }

    @Test
    void hidesExcessiveNestingWhileKeepingOuterTarget() {
        Object nested = Map.of("token", "synthetic-deep");
        for (int depth = 0; depth < 80; depth++) {
            nested = Map.of("nested", nested);
        }
        String source = JsonUtil.toJson(Map.of("id", 29, "payload", nested));

        String result = LogSanitizer.sanitize(source, 16000);

        assertFalse(result.contains("synthetic-deep"), "过深嵌套不得回退为未脱敏内容");
        assertEquals(29, JsonUtil.toMap(result).get("id"));
    }

    @Test
    void masksCredentialFieldsIgnoringCase() {
        String source = "{\"account\":\"eva\",\"newPassword\":\"plain\",\"accessToken\":\"token-value\"}";

        String result = LogSanitizer.sanitize(source, 1000);

        assertFalse(result.contains("plain"));
        assertFalse(result.contains("token-value"));
        assertTrue(result.contains("******"));
    }

    @Test
    void truncatesOversizedPayload() {
        String result = LogSanitizer.sanitize("1234567890", 5);

        assertEquals("12345...[TRUNCATED]", result);
    }

    @Test
    void masksSensitiveUrlQueryParameters() {
        String source = "GET /callback?token=t1&password=p1&apiKey=k1&authorization=Bearer-x&cookie=session-x&safe=ok";

        String result = LogSanitizer.sanitize(source, 1000);

        assertFalse(result.contains("t1"));
        assertFalse(result.contains("p1"));
        assertFalse(result.contains("k1"));
        assertFalse(result.contains("Bearer-x"));
        assertFalse(result.contains("session-x"));
        assertTrue(result.contains("safe=ok"));
    }

    @Test
    void masksAuthorizationHeaderInPlainText() {
        String source = "Authorization: Bearer raw-token GET /sys/account/list";

        String result = LogSanitizer.sanitize(source, 1000);

        assertFalse(result.contains("raw-token"));
        assertTrue(result.contains("Authorization: ******"));
        assertTrue(result.contains("GET /sys/account/list"));
    }
}
