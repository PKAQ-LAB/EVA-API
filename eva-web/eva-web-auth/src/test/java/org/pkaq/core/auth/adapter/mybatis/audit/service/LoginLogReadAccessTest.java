package org.pkaq.core.auth.adapter.mybatis.audit.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.pkaq.core.auth.adapter.mybatis.audit.mapper.LoginLogSqlProvider;
import org.pkaq.core.auth.authorization.service.ResourceAuthorizationService;
import org.pkaq.core.auth.spi.IOpenApiAuditAdministratorQuery;
import org.pkaq.core.auth.spi.ITenantAuthRouter;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 身份、开关与固定租户 SQL 的隔离测试，不接开发数据库。
 * @author PKAQ
 * @date 2026-10-30
 */
class LoginLogReadAccessTest {
    private final EvaConfig config = new EvaConfig();
    private final LoginLogStorage storage = mock(LoginLogStorage.class);
    private final IOpenApiAuditAdministratorQuery administrators = mock(IOpenApiAuditAdministratorQuery.class);
    private final ITenantAuthRouter router = mock(ITenantAuthRouter.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final ResourceAuthorizationService resources = mock(ResourceAuthorizationService.class);
    private final LoginLogReadAccess access = new LoginLogReadAccess(config, storage, administrators,
            router, jdbc, resources);

    @BeforeEach
    void storageBoundary() {
        when(storage.read(any())).thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());
    }

    @AfterEach
    void clear() {
        TenantContext.clear();
    }

    @ParameterizedTest
    @CsvSource({"true,true", "true,false", "false,true", "false,false"})
    void ordinaryAndForgedAdminAreDeniedWithAllSwitches(boolean data, boolean resource) {
        config.getDataPermission().setEnable(data);
        config.getResourcePermission().setEnable(resource);
        ThreadUser user = user(0L).setRolesMap(
                Map.of(9L, new ThreadUser.GrantedRoles("伪管理员", "ROLE_ADMIN")));
        ThreadUserHelper.runWithUser(user, () -> {
            assertThrows(RuntimeException.class, () -> access.read("/monitor/log/login/list", null,
                    () -> { throw new AssertionError("不得读取日志"); }));
            assertThrows(RuntimeException.class, () -> access.read("/monitor/log/login/get/3", null,
                    () -> { throw new AssertionError("不得读取日志"); }));
        });
        verify(resources, never()).hasPermission(anyLong(), any(), anyString(), anyString());
    }

    @ParameterizedTest
    @CsvSource({"true,true", "true,false", "false,true", "false,false"})
    void activeStandaloneAdminUsesTenantZeroForBothReads(boolean data, boolean resource) {
        config.getDataPermission().setEnable(data);
        config.getResourcePermission().setEnable(resource);
        when(administrators.isActiveAdministrator(7L)).thenReturn(true);
        when(resources.hasPermission(anyLong(), any(), eq("GET"), anyString())).thenReturn(true);
        ThreadUserHelper.runWithUser(user(0L), () -> {
            assertTrue(access.read("/monitor/log/login/list", null, LoginLogSqlProvider::page)
                    .contains("TENANT_ID = 0"));
            assertTrue(access.read("/monitor/log/login/get/3", null, LoginLogSqlProvider::detail)
                    .contains("TENANT_ID = 0"));
            assertThrows(RuntimeException.class, () -> access.read("/monitor/log/login/list", 8L,
                    LoginLogSqlProvider::page));
            assertThrows(RuntimeException.class, LoginLogSqlProvider::detail);
        });
    }

    @Test
    void onlyRealPlatformTenantZeroHasGlobalScope() {
        schema(CommonConstant.MODE_PLATFORM);
        config.getResourcePermission().setEnable(false);
        when(administrators.isActiveAdministrator(7L)).thenReturn(true);
        ThreadUserHelper.runWithUser(user(0L), () -> {
            assertFalse(access.read("/monitor/log/login/list", null, LoginLogSqlProvider::page)
                    .contains("TENANT_ID ="));
            assertTrue(access.read("/monitor/log/login/list", 99L, LoginLogSqlProvider::page)
                    .contains("TENANT_ID = 99"));
            assertFalse(access.read("/monitor/log/login/get/3", null, LoginLogSqlProvider::detail)
                    .contains("TENANT_ID ="));
            assertThrows(RuntimeException.class, () -> access.read("/monitor/log/login/list", -1L,
                    LoginLogSqlProvider::page));
        });
    }

    @Test
    void privateMembershipUsesOnlyTrustedTenantAndBothReadsAreFiltered() {
        schema(CommonConstant.MODE_PLATFORM);
        config.getResourcePermission().setEnable(false);
        privateAdministrator(true);
        ThreadUser subject = user(23L).setRolesMap(
                Map.of(9L, new ThreadUser.GrantedRoles("同名角色", "ROLE_ADMIN")));
        ThreadUserHelper.runWithUser(subject, () -> {
            assertTrue(access.read("/monitor/log/login/list", null, LoginLogSqlProvider::page)
                    .contains("TENANT_ID = 23"));
            assertTrue(access.read("/monitor/log/login/get/3", null, LoginLogSqlProvider::detail)
                    .contains("TENANT_ID = 23"));
            assertTrue(access.read("/monitor/log/login/list", 23L, LoginLogSqlProvider::page)
                    .contains("TENANT_ID = 23"));
            assertThrows(RuntimeException.class, () -> access.read("/monitor/log/login/list", 24L,
                    LoginLogSqlProvider::page));
        });
        verify(administrators, never()).isActiveAdministrator(anyLong());
    }

    @Test
    void inactivePrivateMembershipCannotBorrowCoreAdminQualification() {
        schema(CommonConstant.MODE_SAAS);
        privateAdministrator(false);
        ThreadUserHelper.runWithUser(user(23L), () -> assertThrows(RuntimeException.class,
                () -> access.read("/monitor/log/login/get/3", null, LoginLogSqlProvider::detail)));
        verify(administrators, never()).isActiveAdministrator(anyLong());
    }

    @Test
    void missingIdentityAndInconsistentContextNeverReachRead() {
        assertThrows(RuntimeException.class, () -> access.read("/monitor/log/login/list", null,
                LoginLogSqlProvider::page));
        TenantContext.bind(99L, "tenant_other");
        ThreadUserHelper.runWithUser(user(0L), () -> assertThrows(RuntimeException.class,
                () -> access.read("/monitor/log/login/list", null, LoginLogSqlProvider::page)));
        verify(storage, never()).read(any());
    }

    @Test
    void actualResourceDenialIsStillRequiredAfterAdministratorCheck() {
        config.getResourcePermission().setEnable(true);
        when(administrators.isActiveAdministrator(7L)).thenReturn(true);
        ThreadUserHelper.runWithUser(user(0L), () -> assertThrows(RuntimeException.class,
                () -> access.read("/monitor/log/login/get/3", null, LoginLogSqlProvider::detail)));
        verify(resources).hasPermission(eq(0L), eq(java.util.List.of()), eq("GET"),
                eq("/monitor/log/login/get/3"));
    }

    @Test
    void missingTrustedTenantMappingDoesNotReadCoreHistory() {
        schema(CommonConstant.MODE_SAAS);
        org.mockito.Mockito.doThrow(new IllegalStateException("synthetic missing mapping"))
                .when(router).execute(eq(23L), any());
        ThreadUserHelper.runWithUser(user(23L), () -> assertThrows(RuntimeException.class,
                () -> access.read("/monitor/log/login/list", null, LoginLogSqlProvider::page)));
        assertThrows(RuntimeException.class, LoginLogSqlProvider::page);
    }

    private void privateAdministrator(boolean active) {
        doAnswer(invocation -> {
            assertEquals(23L, (Long) invocation.getArgument(0));
            TenantContext.bind(23L, "tenant_trusted");
            try {
                return ((Supplier<?>) invocation.getArgument(1)).get();
            } finally {
                TenantContext.clear();
            }
        }).when(router).execute(eq(23L), any());
        when(jdbc.queryForObject(anyString(), eq(Boolean.class), eq(7L))).thenReturn(active);
    }

    private void schema(String mode) {
        config.setMode(mode);
        config.getTenant().setEnable(true);
        config.getTenant().setMode("schema");
    }

    private ThreadUser user(long tenantId) {
        return new ThreadUser().setUserId(7L).setTenantId(tenantId).setRolesMap(Map.of());
    }
}
