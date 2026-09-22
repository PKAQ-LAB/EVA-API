package org.pkaq.core.log.util;

import org.junit.jupiter.api.Test;
import org.pkaq.core.advice.ExceptionInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * 错误指纹归一化测试。
 *
 * @author PKAQ
 */
class ErrorFingerprintTest {

    @Test
    void normalizesDynamicNumbersAndUuids() {
        ExceptionInfo first = incident("order 123 failed 550e8400-e29b-41d4-a716-446655440000");
        ExceptionInfo second = incident("order 987 failed 123e4567-e89b-12d3-a456-426614174000");

        assertEquals(ErrorFingerprint.calculate(first), ErrorFingerprint.calculate(second));
    }

    @Test
    void separatesDifferentCodeLocations() {
        ExceptionInfo first = incident("order 123 failed");
        ExceptionInfo second = incident("order 123 failed").setMethod("updateOrder");

        assertNotEquals(ErrorFingerprint.calculate(first), ErrorFingerprint.calculate(second));
    }

    private ExceptionInfo incident(String summary) {
        return new ExceptionInfo()
                .setExceptionType(IllegalStateException.class.getName())
                .setClassName("org.pkaq.OrderService")
                .setMethod("saveOrder")
                .setSummary(summary);
    }
}
