package org.pkaq.core.log.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 独立验证注册请求体进入通用日志工具后密码脱敏。
 *
 * @author Codex
 * @date 2026-10-08
 */
class RegistrationBodySanitizerReviewTest {
    /** JSON转义引号不得截断密码字段匹配而泄露余下字符。 */
    @Test
    void masksRegistrationPasswordWithEscapedCharacters() {
        String body = "{\"account\":\"safe-account\",\"password\":\"Test\\\"Pass123!\","
                + "\"nickName\":\"公开昵称\"}";
        String result = LogSanitizer.sanitize(body, 1000);
        assertFalse(result.contains("Test"));
        assertFalse(result.contains("Pass123"));
        assertTrue(result.contains("******"));
        assertTrue(result.contains("safe-account"));
        assertTrue(result.contains("公开昵称"));
    }
}
