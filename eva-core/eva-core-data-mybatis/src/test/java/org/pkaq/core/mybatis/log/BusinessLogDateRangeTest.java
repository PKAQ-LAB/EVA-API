package org.pkaq.core.mybatis.log;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.pkaq.core.log.bo.LogQueryBo;
import org.pkaq.core.mybatis.log.mapper.BusinessLogMapper;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.validation.DataBinder;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * 日期绑定、真实Repository参数和Mapper SQL的结束日边界回归，不连接数据库。
 * @author PKAQ
 * @date 2026-10-23
 */
class BusinessLogDateRangeTest {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void includesFullSelectedDayInHotArchiveCountAndPageAcrossCalendarBoundaries() throws Exception {
        Configuration configuration = new com.baomidou.mybatisplus.core.MybatisConfiguration();
        try (InputStream input = getClass().getResourceAsStream("/mapper/BusinessLogMapper.xml")) {
            new XMLMapperBuilder(input, configuration, "mapper/BusinessLogMapper.xml",
                    configuration.getSqlFragments()).parse();
        }
        for (String selected : List.of("2026-10-23", "2026-01-31", "2026-12-31", "2024-02-29")) {
            LocalDate day = LocalDate.parse(selected);
            Object[] arguments = query(bind(Map.of("begin", selected, "end", selected)));
            assertEquals(FORMAT.format(day.atStartOfDay()), arguments[1]);
            assertEquals(FORMAT.format(day.plusDays(1).atStartOfDay()), arguments[2]);
            assertEquals(true, arguments[3]);
            LocalDateTime lower = LocalDateTime.parse((String) arguments[1], FORMAT);
            LocalDateTime upper = LocalDateTime.parse((String) arguments[2], FORMAT);
            // 与下方实际SQL的>=和<操作符配对，检查数据库微秒边界而非23:59:59截断。
            assertTrue(inRange(day.atStartOfDay(), lower, upper));
            assertTrue(inRange(day.plusDays(1).atStartOfDay().minusNanos(1000), lower, upper));
            assertFalse(inRange(day.plusDays(1).atStartOfDay(), lower, upper));
            assertFalse(inRange(day.atStartOfDay().minusNanos(1000), lower, upper));
            for (String method : List.of("countLogs", "selectLogPage")) {
                for (boolean archived : List.of(false, true)) {
                    Map<String, Object> parameters = parameters(arguments, archived);
                    BoundSql bound = configuration.getMappedStatement(
                            BusinessLogMapper.class.getName() + "." + method).getBoundSql(parameters);
                    String sql = bound.getSql().replaceAll("\\s+", " ");
                    assertTrue(sql.contains("OPERATE_DATETIME < ?"));
                    assertFalse(sql.contains("OPERATE_DATETIME <= ?"));
                    assertTrue(sql.contains("OPERATE_DATETIME >= ?"));
                    long upperCount = bound.getParameterMappings().stream()
                            .filter(parameter -> "end".equals(parameter.getProperty())).count();
                    assertEquals(archived ? 2L : 1L, upperCount);
                    assertEquals(archived, sql.contains("LOG_BIZ_ARCHIVE"));
                    parameters.put("endExclusive", false);
                    String defaultSql = configuration.getMappedStatement(
                            BusinessLogMapper.class.getName() + "." + method).getBoundSql(parameters)
                            .getSql().replaceAll("\\s+", " ");
                    assertTrue(defaultSql.contains("OPERATE_DATETIME <= ?"));
                }
            }
        }
    }

    @Test
    void retainsCurrentInstantDefaultAndSupportsEitherMissingDate() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(2);
        Object[] noDates = query(null);
        assertEquals(false, noDates[3]);
        LocalDateTime nowBound = LocalDateTime.parse((String) noDates[2], FORMAT);
        assertTrue(!nowBound.isBefore(before) && !nowBound.isAfter(LocalDateTime.now()));
        Object[] onlyBegin = query(bind(Map.of("begin", "2026-10-23")));
        assertEquals("2026-10-23 00:00:00", onlyBegin[1]);
        assertEquals(false, onlyBegin[3]);
        Object[] onlyEnd = query(bind(Map.of("end", "2026-10-23")));
        assertEquals("2026-10-24 00:00:00", onlyEnd[2]);
        assertEquals(true, onlyEnd[3]);
    }

    @Test
    void advancesCalendarDaysRatherThanTwentyFourHoursAcrossDst() throws Exception {
        TimeZone previous = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
            for (String date : List.of("2026-03-08", "2026-11-01")) {
                LogQueryBo bo = bind(Map.of("end", date));
                Object[] arguments = query(bo);
                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long hours = (format.parse((String) arguments[2]).getTime() - bo.getEnd().getTime()) / 3_600_000L;
                assertEquals(date.contains("03-08") ? 23L : 25L, hours);
            }
        } finally {
            TimeZone.setDefault(previous);
        }
    }

    private boolean inRange(LocalDateTime value, LocalDateTime lower, LocalDateTime upper) {
        return !value.isBefore(lower) && value.isBefore(upper);
    }

    private Map<String, Object> parameters(Object[] arguments, boolean archived) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", arguments[0]);
        parameters.put("begin", arguments[1]);
        parameters.put("end", arguments[2]);
        parameters.put("endExclusive", arguments[3]);
        parameters.put("includeArchived", archived);
        parameters.put("offset", 0L);
        parameters.put("limit", 10L);
        return parameters;
    }

    private LogQueryBo bind(Map<String, String> dates) {
        LogQueryBo bo = new LogQueryBo();
        DataBinder binder = new DataBinder(bo);
        binder.setConversionService(new DefaultFormattingConversionService());
        binder.bind(new MutablePropertyValues(dates));
        assertFalse(binder.getBindingResult().hasErrors());
        return bo;
    }

    private Object[] query(LogQueryBo bo) {
        List<Object[]> calls = new ArrayList<>();
        BusinessLogMapper mapper = mock(BusinessLogMapper.class, invocation -> {
            if ("countLogs".equals(invocation.getMethod().getName())) {
                calls.add(invocation.getArguments());
                return 0L;
            }
            if ("selectLogPage".equals(invocation.getMethod().getName())) {
                calls.add(invocation.getArguments());
                return List.of();
            }
            return null;
        });
        ThreadUser user = new ThreadUser().setUserId(11L).setTenantId(12L)
                .setDataScopes(List.of(new ThreadUser.DataScope("0000", List.of())));
        ThreadUserHelper.runWithUser(user, () -> new PostgresBusinessLogRepository(mapper, new EvaConfig()).list(bo));
        assertEquals(2, calls.size());
        for (int index = 0; index < 4; index++) {
            assertEquals(calls.get(0)[index], calls.get(1)[index]);
        }
        return calls.get(0);
    }
}
