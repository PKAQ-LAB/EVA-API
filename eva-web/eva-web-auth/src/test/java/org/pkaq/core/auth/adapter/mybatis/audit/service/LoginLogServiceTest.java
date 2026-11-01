package org.pkaq.core.auth.adapter.mybatis.audit.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.adapter.mybatis.audit.entity.LoginLogEntity;
import org.pkaq.core.auth.adapter.mybatis.audit.mapper.LoginLogMapper;
import org.pkaq.core.auth.audit.bo.LoginLogQueryBo;
import org.pkaq.core.mybatis.log.LoginLogQueryScope;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.web.core.client.ClientInfoResolver;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 外层 best-effort、动作事务粒度及历史入口委托的隔离验证。
 * @author PKAQ
 * @date 2026-10-30
 */
class LoginLogServiceTest {
    private final LoginLogMapper mapper = mock(LoginLogMapper.class);
    private final LoginLogStorage storage = mock(LoginLogStorage.class);
    private final LoginLogReadAccess access = mock(LoginLogReadAccess.class);
    private final LoginLogService service = new LoginLogService(mapper,
            new ClientInfoResolver(new EvaConfig()), storage, access);

    @BeforeEach
    void initialize() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), LoginLogEntity.class);
        doAnswer(invocation -> {
            LoginLogQueryScope.write(invocation.getArgument(0));
            return null;
        }).when(storage).write(any());
    }

    @Test
    void successRiskAndInsertUseOneStorageAction() {
        when(mapper.selectRiskDeviceCount(eq(45L), eq(67L), any())).thenReturn(0L);
        when(mapper.selectRiskLastLogin(45L, 67L)).thenAnswer(invocation -> {
            LoginLogQueryScope.requireStorage(LoginLogMapper.class.getName() + ".selectRiskLastLogin");
            return null;
        });
        doAnswer(invocation -> {
            LoginLogEntity entity = invocation.getArgument(0);
            assertEquals(45L, entity.getTenantId());
            assertEquals(67L, entity.getUserId());
            assertEquals("web-1", entity.getSessionId());
            assertEquals("203.0.113.10", entity.getIp());
            assertEquals("Windows", entity.getOsName());
            assertEquals("Edge", entity.getBrowserName());
            assertTrue(entity.getRiskFlags().contains("NEW_DEVICE"));
            return 1;
        }).when(mapper).insert(any(LoginLogEntity.class));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.10");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0");
        service.saveSuccess(request, 45L, 67L, "synthetic", "web-1");
        verify(storage).write(any());
        var order = inOrder(mapper);
        order.verify(mapper).selectRiskDeviceCount(eq(45L), eq(67L), any());
        order.verify(mapper).selectRiskLastLogin(45L, 67L);
        order.verify(mapper).insert(any(LoginLogEntity.class));
    }

    @Test
    void logoutCloseAndInsertUseOneStorageAction() {
        service.saveLogout(new MockHttpServletRequest(), 45L, 67L, "synthetic", "web-1", "USER_LOGOUT");
        verify(storage).write(any());
        var order = inOrder(mapper);
        order.verify(mapper).update(eq(null), any());
        order.verify(mapper).insert(any(LoginLogEntity.class));
    }

    @Test
    void logoutOverloadKeepsCurrentTrustedIdentity() {
        org.pkaq.core.threaduser.ThreadUser subject = new org.pkaq.core.threaduser.ThreadUser()
                .setUserId(67L).setTenantId(45L).setAccount("synthetic");
        doAnswer(invocation -> {
            LoginLogEntity entity = invocation.getArgument(0);
            assertEquals(45L, entity.getTenantId());
            assertEquals(67L, entity.getUserId());
            assertEquals("synthetic", entity.getAccount());
            return 1;
        }).when(mapper).insert(any(LoginLogEntity.class));
        org.pkaq.core.threaduser.ThreadUserHelper.runWithUser(subject,
                () -> service.saveLogout(new MockHttpServletRequest()));
        verify(storage).write(any());
        verify(mapper).insert(any(LoginLogEntity.class));
    }

    @Test
    void independentSessionUpdatesUseStorage() {
        service.closeSession(45L, 67L, "old", "USER_LOGOUT");
        service.rotateSession(45L, 67L, "old", "new");
        service.closeUserSessions(45L, 67L, "ADMIN_FORCED");
        verify(storage, times(3)).write(any());
        verify(mapper, times(3)).update(eq(null), any());
    }

    @Test
    void proxyCommitFailureNeverEscapesAuditFacade() {
        doThrow(new org.springframework.transaction.TransactionSystemException("synthetic commit failure"))
                .when(storage).write(any());
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertDoesNotThrow(() -> service.saveSuccess(request, 1L, 2L, "synthetic", "session"));
        assertDoesNotThrow(() -> service.saveFailure(request, "synthetic"));
        assertDoesNotThrow(() -> service.saveLogout(request, 1L, 2L, "synthetic", "session", "USER_LOGOUT"));
        assertDoesNotThrow(() -> service.closeSession(1L, 2L, "session", "USER_LOGOUT"));
        assertDoesNotThrow(() -> service.rotateSession(1L, 2L, "old", "new"));
        assertDoesNotThrow(() -> service.closeUserSessions(1L, 2L, "ADMIN_FORCED"));
    }

    @Test
    void detailAndPageDelegateToGuardWithActualPaths() {
        doAnswer(invocation -> ((Supplier<?>) invocation.getArgument(2)).get())
                .when(access).read(anyString(), any(), any());
        when(mapper.selectHistoryPage(any(), any())).thenReturn(new Page<>());
        service.get(3L);
        service.list(new LoginLogQueryBo());
        verify(access).read(eq("/monitor/log/login/get/3"), eq(null), any());
        verify(access).read(eq("/monitor/log/login/list"), eq(null), any());
        verify(mapper).selectHistoryDetail(3L);
    }
}
