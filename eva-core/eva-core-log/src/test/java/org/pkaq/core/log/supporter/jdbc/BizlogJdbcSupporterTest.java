package org.pkaq.core.log.supporter.jdbc;

import org.junit.jupiter.api.Test;
import org.pkaq.core.log.base.BizLogEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class BizlogJdbcSupporterTest {
    @Test
    void savesBusinessLogWithJdbc() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        BizlogJdbcSupporter supporter = new BizlogJdbcSupporter(jdbcTemplate);
        BizLogEntity entity = new BizLogEntity().setOperator("tester").setOperateType("UPDATE")
                .setOperateDatetime("2026-08-26 10:00:00").setDescription("test");
        supporter.save(entity);
        verify(jdbcTemplate).update(anyString(), any(Object[].class));
    }

    @Test
    void ignoresNullLog() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        BizlogJdbcSupporter supporter = new BizlogJdbcSupporter(jdbcTemplate);
        supporter.save(null);
        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }
}
