package org.pkaq.core.cache.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis 游标扫描测试。
 *
 * @author PKAQ
 */
@ExtendWith(MockitoExtension.class)
class RedisUtilTest {
    @Mock
    private RedisTemplate<Object, Object> redisTemplate;
    @Mock
    private ValueOperations<Object, Object> valueOperations;
    @Mock
    private Cursor<Object> cursor;

    @Test
    void scansOnlyCompleteTenantCachePrefixWithoutKeysCommand() {
        when(this.redisTemplate.scan(any(ScanOptions.class))).thenReturn(this.cursor);
        doAnswer(invocation -> {
            java.util.function.Consumer<Object> consumer = invocation.getArgument(0);
            consumer.accept("token::7:11");
            return null;
        }).when(this.cursor).forEachRemaining(any());
        when(this.redisTemplate.opsForValue()).thenReturn(this.valueOperations);
        when(this.valueOperations.get("token::7:11")).thenReturn(Map.of("device", "web"));
        RedisUtil redisUtil = new RedisUtil(this.redisTemplate);

        Map<String, ?> result = redisUtil.scanPureAll("token", "7:");

        assertEquals(Map.of("device", "web"), result.get("7:11"));
        ArgumentCaptor<ScanOptions> optionsCaptor = ArgumentCaptor.forClass(ScanOptions.class);
        verify(this.redisTemplate).scan(optionsCaptor.capture());
        assertEquals("token::7:*", optionsCaptor.getValue().getPattern());
        verify(this.redisTemplate, never()).keys(any());
    }
}
