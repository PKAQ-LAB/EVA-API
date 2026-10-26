package org.pkaq.sys.log.error.ctrl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mybatis.exception.PostgresErrorIncidentRepository;
import org.pkaq.core.mybatis.exception.entity.ErrorIncidentEntity;
import org.pkaq.core.mybatis.exception.mapper.ErrorIncidentMapper;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.DatePatterns;
import org.pkaq.core.util.DateUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.validation.DataBinder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 实际Controller到当前Repository的缺省日期回归，仅Mapper为内存替身。
 * @author PKAQ
 * @date 2026-10-23
 */
class ErrorCtrlDateRangeTest {
    private ErrorIncidentMapper mapper;
    private ErrorCtrl controller;

    @BeforeEach
    void setUp() {
        mapper = mock(ErrorIncidentMapper.class);
        EvaConfig config = new EvaConfig();
        config.setMode(CommonConstant.MODE_SAAS);
        controller = new ErrorCtrl(new PostgresErrorIncidentRepository(mapper, config));
    }

    @Test
    void omittedEndRetainsNowInclusiveAndOriginalDefaultBeginAndPagination() {
        DateRangeBo range = bind(Map.of());
        Date before = new Date();

        ThreadUserHelper.runWithUser(user("0000"), () -> controller.list(range, null, null));

        assertNull(range.getEnd());
        QueryWrapper<ErrorIncidentEntity> wrapper = wrapper(1, 10);
        assertNowInclusive(wrapper, before);
        assertTrue(range.getBegin().getTime() >= DateUtils.addDay(before, -7).getTime());
        assertTrue(range.getBegin().getTime() <= DateUtils.addDay(new Date(), -7).getTime());
    }

    @Test
    void onlyBeginPreservesExplicitBeginAndNowInclusiveWithCustomPagination() {
        DateRangeBo range = bind(Map.of("begin", "2026-10-23"));
        Date before = new Date();

        ThreadUserHelper.runWithUser(user("0000"), () -> controller.list(range, 3, 7));

        assertNull(range.getEnd());
        QueryWrapper<ErrorIncidentEntity> wrapper = wrapper(3, 7);
        assertNowInclusive(wrapper, before);
        assertTrue(wrapper.getParamNameValuePairs().containsValue("2026-10-23 00:00:00"));
    }

    @Test
    void explicitEndUsesNextDayExclusiveAndPreservesInclusiveBegin() {
        DateRangeBo range = bind(Map.of("begin", "2026-12-31", "end", "2026-12-31"));

        ThreadUserHelper.runWithUser(user("0000"), () -> controller.list(range, null, null));

        QueryWrapper<ErrorIncidentEntity> wrapper = wrapper(1, 10);
        assertTrue(wrapper.getSqlSegment().contains("request_time < "));
        assertFalse(wrapper.getSqlSegment().contains("request_time <= "));
        assertTrue(wrapper.getParamNameValuePairs().containsValue("2027-01-01 00:00:00"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue("2026-12-31 00:00:00"));
        assertEquals("2026-12-31", DateUtils.format(range.getEnd(), "yyyy-MM-dd"));
    }

    @Test
    void actualControllerChainRejectsAnonymousAndRestrictedIdentityBeforeMapper() {
        assertThrows(RuntimeException.class, () -> controller.list(bind(Map.of()), null, null));
        ThreadUserHelper.runWithUser(user("0007"), () -> assertThrows(RuntimeException.class,
                () -> controller.list(bind(Map.of("end", "2026-10-23")), 1, 10)));
        verifyNoInteractions(mapper);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private QueryWrapper<ErrorIncidentEntity> wrapper(long pageNo, long pageSize) {
        ArgumentCaptor<QueryWrapper<ErrorIncidentEntity>> query = ArgumentCaptor.forClass(QueryWrapper.class);
        ArgumentCaptor<IPage> page = ArgumentCaptor.forClass(IPage.class);
        verify(mapper).selectPage(page.capture(), query.capture());
        assertEquals(pageNo, page.getValue().getCurrent());
        assertEquals(pageSize, page.getValue().getSize());
        QueryWrapper<ErrorIncidentEntity> result = query.getValue();
        assertTrue(result.getSqlSegment().contains("request_time >= "));
        assertTrue(result.getParamNameValuePairs().containsValue(34L));
        return result;
    }

    private void assertNowInclusive(QueryWrapper<ErrorIncidentEntity> wrapper, Date before) {
        String sql = wrapper.getSqlSegment();
        assertTrue(sql.contains("request_time <= "));
        String prefix = "request_time <= #{ew.paramNameValuePairs.";
        int start = sql.indexOf(prefix) + prefix.length();
        String key = sql.substring(start, sql.indexOf('}', start));
        String actual = (String) wrapper.getParamNameValuePairs().get(key);
        String lower = DateUtils.format(before, DatePatterns.NORM_DATETIME_PATTERN);
        String upper = DateUtils.format(new Date(), DatePatterns.NORM_DATETIME_PATTERN);
        // 真实列为VARCHAR(32)，与既有固定宽度字符串比较契约一致。
        assertTrue(actual.compareTo(lower) >= 0 && actual.compareTo(upper) <= 0);
    }

    private DateRangeBo bind(Map<String, String> values) {
        DateRangeBo range = new DateRangeBo();
        DataBinder binder = new DataBinder(range);
        binder.setConversionService(new DefaultFormattingConversionService());
        binder.bind(new MutablePropertyValues(values));
        assertFalse(binder.getBindingResult().hasErrors());
        return range;
    }

    private ThreadUser user(String code) {
        return new ThreadUser().setUserId(11L).setTenantId(34L)
                .setDataScopes(List.of(new ThreadUser.DataScope(code, List.of())));
    }
}
