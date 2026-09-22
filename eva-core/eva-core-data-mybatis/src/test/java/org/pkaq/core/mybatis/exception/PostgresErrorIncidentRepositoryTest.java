package org.pkaq.core.mybatis.exception;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.log.base.ErrorIncident;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mybatis.exception.entity.ErrorIncidentEntity;
import org.pkaq.core.mybatis.exception.mapper.ErrorIncidentMapper;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

/**
 * MyBatis 错误日志租户隔离测试。
 *
 * @author PKAQ
 */
@ExtendWith(MockitoExtension.class)
class PostgresErrorIncidentRepositoryTest {
    @Mock
    private ErrorIncidentMapper errorIncidentMapper;

    private EvaConfig evaConfig;

    private PostgresErrorIncidentRepository repository;

    /**
     * 初始化被测服务。
     */
    @BeforeEach
    void setUp() {
        this.evaConfig = new EvaConfig();
        this.repository = new PostgresErrorIncidentRepository(this.errorIncidentMapper, this.evaConfig);
    }

    /**
     * 已登录用户只能读取当前租户的错误日志。
     */
    @Test
    void shouldFilterGetByCurrentTenant() {
        this.evaConfig.setMode(CommonConstant.MODE_SAAS);

        ThreadUserHelper.runWithUser(new ThreadUser().setTenantId(12L), () -> this.repository.get("error-1"));

        assertTenantValue(captureGetWrapper(), 12L);
    }

    /**
     * 未登录请求不得省略租户过滤。
     */
    @Test
    void shouldDenyUnauthenticatedGet() {
        this.evaConfig.setMode(CommonConstant.MODE_PLATFORM);

        this.repository.get("error-1");

        assertTenantValue(captureGetWrapper(), -1L);
    }

    /**
     * 单机模式必须固定使用零号租户。
     */
    @Test
    void shouldUseTenantZeroInStandaloneMode() {
        this.evaConfig.setMode(CommonConstant.MODE_STANDALONE);

        this.repository.get("error-1");

        assertTenantValue(captureGetWrapper(), 0L);
    }

    /**
     * 列表查询必须使用当前可信租户。
     */
    @Test
    void shouldFilterListByCurrentTenant() {
        this.evaConfig.setMode(CommonConstant.MODE_SAAS);

        ThreadUserHelper.runWithUser(new ThreadUser().setTenantId(34L),
                () -> this.repository.list(new DateRangeBo(), 1, 10));

        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<QueryWrapper<ErrorIncidentEntity>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(this.errorIncidentMapper).selectPage(any(), captor.capture());
        assertTenantValue(captor.getValue(), 34L);
    }

    /**
     * 保存错误事件必须使用指纹聚合写入。
     */
    @Test
    void shouldUpsertIncidentByFingerprint() {
        ErrorIncident incident = new ErrorIncident();
        incident.setTenantId(21L);
        incident.setFingerprint("fingerprint");

        this.repository.save(incident);

        ArgumentCaptor<ErrorIncidentEntity> captor = ArgumentCaptor.forClass(ErrorIncidentEntity.class);
        verify(this.errorIncidentMapper).upsert(captor.capture());
        assertEquals("fingerprint", captor.getValue().getFingerprint());
        assertTrue(captor.getValue().getId() > 0L);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private QueryWrapper<ErrorIncidentEntity> captureGetWrapper() {
        ArgumentCaptor<QueryWrapper<ErrorIncidentEntity>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(this.errorIncidentMapper).selectOne(captor.capture());
        return captor.getValue();
    }

    private void assertTenantValue(QueryWrapper<ErrorIncidentEntity> wrapper, long tenantId) {
        assertTrue(wrapper.getSqlSegment().toLowerCase().contains("tenant_id"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(tenantId));
    }
}
