package org.pkaq.core.log.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogSanitizerTest {

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
