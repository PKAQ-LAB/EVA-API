package org.pkaq.core.log.bo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.validation.DataBinder;

import java.text.SimpleDateFormat;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 日志查询日期参数绑定回归。
 *
 * @author Codex
 * @date 2026-10-18
 */
class LogQueryBoTest {
    @Test
    void bindsCalendarDateRange() {
        LogQueryBo query = new LogQueryBo();
        DataBinder binder = new DataBinder(query);
        binder.setConversionService(new DefaultFormattingConversionService());

        binder.bind(new MutablePropertyValues(Map.of("begin", "2026-10-11", "end", "2026-10-18")));

        assertFalse(binder.getBindingResult().hasErrors());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals("2026-10-11", format.format(query.getBegin()));
        assertEquals("2026-10-18", format.format(query.getEnd()));
    }

    @Test
    void rejectsInvalidDate() {
        DataBinder binder = new DataBinder(new LogQueryBo());
        binder.setConversionService(new DefaultFormattingConversionService());

        binder.bind(new MutablePropertyValues(Map.of("begin", "not-a-date")));

        assertTrue(binder.getBindingResult().hasFieldErrors("begin"));
    }
}
