package org.pkaq.core.auth.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pkaq.core.auth.log.bo.LoginLogQueryBo;
import org.pkaq.core.auth.log.entity.LoginLogEntity;
import org.pkaq.core.auth.log.mapper.LoginLogMapper;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

/**
 * 登录日志租户隔离测试。
 *
 * @author PKAQ
 */
@ExtendWith(MockitoExtension.class)
class LoginLogServiceTest {
    @Mock
    private LoginLogMapper loginLogMapper;

    private EvaConfig evaConfig;

    private LoginLogService service;

    /**
     * 初始化被测服务。
     */
    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                LoginLogEntity.class);
        this.evaConfig = new EvaConfig();
        this.service = new LoginLogService(this.loginLogMapper, this.evaConfig);
        lenient().when(this.loginLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<>());
    }

    /**
     * 普通租户不得通过目标租户参数越权查询。
     */
    @Test
    void shouldIgnoreTargetTenantForTenantUser() {
        this.evaConfig.setMode(CommonConstant.MODE_SAAS);
        LoginLogQueryBo queryBo = new LoginLogQueryBo();
        queryBo.setTargetTenantId(99L);

        ThreadUserHelper.runWithUser(user(7L, false), () -> this.service.list(queryBo));

        assertTenantValue(captureListWrapper(), 7L);
    }

    /**
     * 平台管理员可以显式查询目标租户。
     */
    @Test
    void shouldAllowPlatformAdminTargetTenant() {
        this.evaConfig.setMode(CommonConstant.MODE_PLATFORM);
        LoginLogQueryBo queryBo = new LoginLogQueryBo();
        queryBo.setTargetTenantId(99L);

        ThreadUserHelper.runWithUser(user(0L, true), () -> this.service.list(queryBo));

        assertTenantValue(captureListWrapper(), 99L);
    }

    /**
     * 平台管理员不指定目标租户时可以查询全局日志。
     */
    @Test
    void shouldAllowPlatformAdminGlobalQuery() {
        this.evaConfig.setMode(CommonConstant.MODE_PLATFORM);

        ThreadUserHelper.runWithUser(user(0L, true), () -> this.service.list(new LoginLogQueryBo()));

        LambdaQueryWrapper<LoginLogEntity> wrapper = captureListWrapper();
        assertFalse(wrapper.getSqlSegment().toLowerCase().contains("tenant_id"));
    }

    /**
     * 未登录请求必须使用拒绝式租户条件。
     */
    @Test
    void shouldDenyUnauthenticatedQuery() {
        this.evaConfig.setMode(CommonConstant.MODE_SAAS);

        this.service.list(new LoginLogQueryBo());

        assertTenantValue(captureListWrapper(), -1L);
    }

    /**
     * 单机模式必须固定使用零号租户。
     */
    @Test
    void shouldUseTenantZeroInStandaloneMode() {
        this.evaConfig.setMode(CommonConstant.MODE_STANDALONE);

        this.service.list(new LoginLogQueryBo());

        assertTenantValue(captureListWrapper(), 0L);
    }

    /**
     * 详情查询必须使用当前可信租户。
     */
    @Test
    void shouldFilterGetByCurrentTenant() {
        this.evaConfig.setMode(CommonConstant.MODE_SAAS);

        ThreadUserHelper.runWithUser(user(23L, false), () -> this.service.get(1L));

        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<LambdaQueryWrapper<LoginLogEntity>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(this.loginLogMapper).selectOne(captor.capture());
        assertTenantValue(captor.getValue(), 23L);
    }

    /**
     * 退出日志必须保存可信用户的租户、用户和账号。
     */
    @Test
    void shouldSaveLogoutIdentity() {
        ThreadUser currentUser = new ThreadUser()
                .setTenantId(45L)
                .setUserId(67L)
                .setAccount("logout-account");

        ThreadUserHelper.runWithUser(currentUser,
                () -> this.service.saveLogout(new org.springframework.mock.web.MockHttpServletRequest()));

        ArgumentCaptor<LoginLogEntity> captor = ArgumentCaptor.forClass(LoginLogEntity.class);
        verify(this.loginLogMapper).insert(captor.capture());
        LoginLogEntity entity = captor.getValue();
        assertEquals(45L, entity.getTenantId());
        assertEquals(67L, entity.getUserId());
        assertEquals("logout-account", entity.getAccount());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private LambdaQueryWrapper<LoginLogEntity> captureListWrapper() {
        ArgumentCaptor<LambdaQueryWrapper<LoginLogEntity>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(this.loginLogMapper).selectPage(any(Page.class), captor.capture());
        return captor.getValue();
    }

    private void assertTenantValue(LambdaQueryWrapper<LoginLogEntity> wrapper, long tenantId) {
        assertTrue(wrapper.getSqlSegment().toLowerCase().contains("tenant_id"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(tenantId));
    }

    private ThreadUser user(long tenantId, boolean admin) {
        ThreadUser user = new ThreadUser().setTenantId(tenantId);
        if (admin) {
            user.setRolesMap(Map.of(1L,
                    new ThreadUser.GrantedRoles("管理员", CommonConstant.ADMIN_ROLE_NAME)));
        }
        return user;
    }
}
