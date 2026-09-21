package org.pkaq.core.mybatis.exception;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mybatis.exception.entity.ErrorlogEntity;
import org.pkaq.core.mybatis.exception.mapper.ErrorlogMapper;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

/**
 * MyBatis 错误日志租户隔离测试。
 *
 * @author PKAQ
 */
@ExtendWith(MockitoExtension.class)
class MybatisErrorLogSupporterTest {
    @Mock
    private ErrorlogMapper errorlogMapper;

    private EvaConfig evaConfig;

    private MybatisErrorLogSupporter supporter;

    /**
     * 初始化被测服务。
     */
    @BeforeEach
    void setUp() {
        this.evaConfig = new EvaConfig();
        this.supporter = new MybatisErrorLogSupporter(this.errorlogMapper, this.evaConfig);
    }

    /**
     * 已登录用户只能读取当前租户的错误日志。
     */
    @Test
    void shouldFilterGetByCurrentTenant() {
        this.evaConfig.setMode(CommonConstant.MODE_SAAS);

        ThreadUserHelper.runWithUser(new ThreadUser().setTenantId(12L), () -> this.supporter.get("error-1"));

        assertTenantValue(captureGetWrapper(), 12L);
    }

    /**
     * 未登录请求不得省略租户过滤。
     */
    @Test
    void shouldDenyUnauthenticatedGet() {
        this.evaConfig.setMode(CommonConstant.MODE_PLATFORM);

        this.supporter.get("error-1");

        assertTenantValue(captureGetWrapper(), -1L);
    }

    /**
     * 单机模式必须固定使用零号租户。
     */
    @Test
    void shouldUseTenantZeroInStandaloneMode() {
        this.evaConfig.setMode(CommonConstant.MODE_STANDALONE);

        this.supporter.get("error-1");

        assertTenantValue(captureGetWrapper(), 0L);
    }

    /**
     * 列表查询必须使用当前可信租户。
     */
    @Test
    void shouldFilterListByCurrentTenant() {
        this.evaConfig.setMode(CommonConstant.MODE_SAAS);

        ThreadUserHelper.runWithUser(new ThreadUser().setTenantId(34L),
                () -> this.supporter.list(new DateRangeBo(), 1, 10));

        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<QueryWrapper<ErrorlogEntity>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(this.errorlogMapper).selectPage(any(), captor.capture());
        assertTenantValue(captor.getValue(), 34L);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private QueryWrapper<ErrorlogEntity> captureGetWrapper() {
        ArgumentCaptor<QueryWrapper<ErrorlogEntity>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(this.errorlogMapper).selectOne(captor.capture());
        return captor.getValue();
    }

    private void assertTenantValue(QueryWrapper<ErrorlogEntity> wrapper, long tenantId) {
        assertTrue(wrapper.getSqlSegment().toLowerCase().contains("tenant_id"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(tenantId));
    }
}
