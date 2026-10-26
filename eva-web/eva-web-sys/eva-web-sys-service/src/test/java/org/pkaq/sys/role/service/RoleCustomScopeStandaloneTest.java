package org.pkaq.sys.role.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.sys.role.bo.RoleAoeBo;
import org.pkaq.sys.role.entity.RoleEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 单体自定义组织范围的真实SQL与事务边界回归，全部使用合成数据及模拟JDBC。
 * @author PKAQ
 * @date 2026-10-24
 */
class RoleCustomScopeStandaloneTest {
    @AfterEach
    void cleanContext() {
        TenantContext.clear();
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertTrue(TransactionSynchronizationManager.getResourceMap().isEmpty());
        assertNull(ThreadUserHelper.getCurrentUserOrNull());
    }

    @ParameterizedTest
    @CsvSource({"true,12", "true,13", "true,'12,13'", "false,'12,13'"})
    void acceptsNullAndZeroOnlyAfterSameConnectionSourceCheck(boolean dataEnabled, String ids) throws Exception {
        CustomScopeJdbcFixture fixture = new CustomScopeJdbcFixture(dataEnabled);
        run(fixture, user("0000"), ids);
        ArgumentCaptor<RoleEntity> saved = ArgumentCaptor.forClass(RoleEntity.class);
        verify(fixture.roleMapper).insertOrUpdate(saved.capture());
        assertEquals("0003", saved.getValue().getDataScope());
        assertEquals(ids, saved.getValue().getDataOrgIds());
        assertNull(saved.getValue().getTenantId());
        assertEquals(1, fixture.schemaQueries.get());
        String sql = fixture.organizationSql.getFirst().toLowerCase();
        assertTrue(sql.matches("(?s).*\\(tenant_id is null or tenant_id = \\?\\).*"), sql);
        assertTrue(sql.contains("deleted=0") || sql.contains("deleted = 0"), sql);
        assertTrue(sql.contains("id in"), sql);
        assertTrue(fixture.parameterBindings.getFirst().containsValue(0L));
        System.out.println("SYNTHETIC_BOUND_SQL=" + fixture.organizationSql.getFirst()
                + "; JDBC_PARAMETERS=" + new java.util.TreeMap<>(fixture.parameterBindings.getFirst()));
        verify(fixture.connection).commit();
        verifyNoInteractions(fixture.roleUserMapper, fixture.roleResourceMapper, fixture.userMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12,14", "12,15", "12,777", "14"})
    void rejectsEntireSetIfAnyOrganizationIsForeignDeletedOrMissing(String ids) throws Exception {
        CustomScopeJdbcFixture fixture = new CustomScopeJdbcFixture(true);
        reject(fixture, user("0000"), ids);
        assertEquals(1, fixture.organizationSql.size());
    }

    @Test
    void preservesRealDataPermissionPluginAndItsExistingSwitch() throws Exception {
        CustomScopeJdbcFixture restricted = new CustomScopeJdbcFixture(true);
        reject(restricted, user("0007"), "12,16");
        assertTrue(restricted.organizationSql.getFirst().toLowerCase().contains("create_id = 7"));
        CustomScopeJdbcFixture own = new CustomScopeJdbcFixture(true);
        run(own, user("0007"), "12,13");
        verify(own.roleMapper).insertOrUpdate(any(RoleEntity.class));
        CustomScopeJdbcFixture empty = new CustomScopeJdbcFixture(true);
        reject(empty, user(null), "12");
        CustomScopeJdbcFixture disabled = new CustomScopeJdbcFixture(false);
        run(disabled, user(null), "12,16");
        verify(disabled.roleMapper).insertOrUpdate(any(RoleEntity.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "0", "-1", "12,no", "12,", ",12", "99999999999999999999999", "12,12"})
    void rejectsInvalidAndDuplicateIdsBeforeSql(String ids) throws Exception {
        CustomScopeJdbcFixture fixture = new CustomScopeJdbcFixture(true);
        reject(fixture, user("0000"), ids);
        assertEquals(0, fixture.schemaQueries.get());
        assertTrue(fixture.organizationSql.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"public,eva", "eva,public", "eva,core", "eva,third", "third,third"})
    void rejectsWrongCurrentOrActualRelationSchema(String current, String table) throws Exception {
        CustomScopeJdbcFixture fixture = new CustomScopeJdbcFixture(true);
        fixture.currentSchema = current;
        fixture.tableSchema = table;
        reject(fixture, user("0000"), "12");
        assertTrue(fixture.organizationSql.isEmpty());
    }

    @Test
    void rejectsMissingRelationAndCatalogExceptionWithoutFallback() throws Exception {
        CustomScopeJdbcFixture missing = new CustomScopeJdbcFixture(true);
        missing.tableSchema = null;
        reject(missing, user("0000"), "12");
        assertTrue(missing.organizationSql.isEmpty());
        CustomScopeJdbcFixture failure = new CustomScopeJdbcFixture(true);
        failure.schemaFailure = true;
        reject(failure, user("0000"), "12");
        assertEquals(1, failure.schemaQueries.get());
        assertTrue(failure.organizationSql.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"0,7", "22,7", "0,0", "0,-1"})
    void rejectsUntrustedIdentity(long tenant, long id) throws Exception {
        if (tenant == 0L && id == 7L) {
            CustomScopeJdbcFixture fixture = new CustomScopeJdbcFixture(false);
            assertThrows(RuntimeException.class, () -> fixture.service.editUniqueCode(bo("12")));
            assertNoWrites(fixture);
            assertEquals(0, fixture.schemaQueries.get());
            return;
        }
        CustomScopeJdbcFixture fixture = new CustomScopeJdbcFixture(false);
        reject(fixture, user("0000").setUserId(id).setTenantId(tenant), "12");
        assertEquals(0, fixture.schemaQueries.get());
    }

    @ParameterizedTest
    @CsvSource({"22,tenant_a", "0,third", "0,NULL", "NULL,eva"})
    void rejectsConflictingOrPartialContextAndPreservesIt(String tenant, String schema) throws Exception {
        Long tenantId = "NULL".equals(tenant) ? null : Long.valueOf(tenant);
        String schemaName = "NULL".equals(schema) ? null : schema;
        TenantContext.bind(tenantId, schemaName);
        CustomScopeJdbcFixture fixture = new CustomScopeJdbcFixture(false);
        reject(fixture, user("0000"), "12");
        assertEquals(tenantId, TenantContext.tenantId());
        assertEquals(schemaName, TenantContext.schemaName());
        assertEquals(0, fixture.schemaQueries.get());
    }

    @Test
    void acceptsSingletonAliasAndMatchingTrustedContext() throws Exception {
        TenantContext.bind(0L, "eva");
        CustomScopeJdbcFixture fixture = new CustomScopeJdbcFixture(true);
        fixture.config.setMode("singleton");
        run(fixture, user("0000"), "12");
        assertEquals(0L, TenantContext.tenantId());
        assertEquals("eva", TenantContext.schemaName());
        verify(fixture.roleMapper).insertOrUpdate(any(RoleEntity.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"public.other", "Eva", "bad;schema", "unknown schema"})
    void rejectsInvalidConfiguredSchema(String schema) throws Exception {
        CustomScopeJdbcFixture fixture = new CustomScopeJdbcFixture(false);
        fixture.config.getTenant().setStandaloneSchema(schema);
        reject(fixture, user("0000"), "12");
        assertEquals(0, fixture.schemaQueries.get());
    }

    @Test
    void rejectsMixedUnknownModeOrEnabledTenancy() throws Exception {
        for (String mode : List.of("standalone", "singleton", "unknown")) {
            CustomScopeJdbcFixture fixture = new CustomScopeJdbcFixture(false);
            fixture.config.setMode(mode);
            fixture.config.getTenant().setEnable(true);
            fixture.config.getTenant().setMode("schema");
            reject(fixture, user("0000"), "12");
            assertEquals(0, fixture.schemaQueries.get());
        }
    }

    @Test
    void rejectsWithoutActualTransactionOrWithUnboundJdbcDatasource() throws Exception {
        CustomScopeJdbcFixture direct = new CustomScopeJdbcFixture(false);
        ThreadUserHelper.runWithUser(user("0000"), () -> assertThrows(RuntimeException.class,
                () -> direct.unproxied.editUniqueCode(bo("12"))));
        assertNoWrites(direct);
        CustomScopeJdbcFixture other = new CustomScopeJdbcFixture(false);
        ReflectionTestUtils.setField(other.unproxied, "jdbcTemplate", new JdbcTemplate(mock(DataSource.class)));
        reject(other, user("0000"), "12");
        assertEquals(0, other.schemaQueries.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"platform", "saas"})
    void positiveModesRetainStrictTenantPredicateWithoutNullExpansion(String mode) throws Exception {
        CustomScopeJdbcFixture valid = new CustomScopeJdbcFixture(false);
        valid.config.setMode(mode);
        run(valid, user("0000").setTenantId(22L), "14");
        assertEquals(0, valid.schemaQueries.get());
        assertFalse(valid.organizationSql.getFirst().toLowerCase().contains("is null"));
        assertTrue(valid.parameterBindings.getFirst().containsValue(22L));
        CustomScopeJdbcFixture foreign = new CustomScopeJdbcFixture(false);
        foreign.config.setMode(mode);
        reject(foreign, user("0000").setTenantId(22L), "12");
    }

    @Test
    void missingPositiveTenantColumnRemainsFailureWithoutFallback() throws Exception {
        CustomScopeJdbcFixture fixture = new CustomScopeJdbcFixture(false);
        fixture.config.setMode("saas");
        fixture.config.getTenant().setEnable(true);
        fixture.config.getTenant().setMode("schema");
        fixture.organizationFailure = true;
        reject(fixture, user("0000").setTenantId(22L), "14");
        assertEquals(0, fixture.schemaQueries.get());
        assertEquals(1, fixture.organizationSql.size());
    }

    @Test
    void mapperFailureRollsBackAndNeverChangesGrantsOrPermissionVersions() throws Exception {
        TenantContext.bind(0L, "eva");
        CustomScopeJdbcFixture fixture = new CustomScopeJdbcFixture(true);
        doThrow(new IllegalStateException("合成写入失败")).when(fixture.roleMapper)
                .insertOrUpdate(any(RoleEntity.class));
        assertThrows(RuntimeException.class, () -> run(fixture, user("0000"), "12"));
        verify(fixture.connection).rollback();
        verifyNoInteractions(fixture.roleUserMapper, fixture.roleResourceMapper, fixture.userMapper);
        assertEquals("eva", TenantContext.schemaName());
    }

    @Test
    void customEditPreservesOmittedOrganizationsAndRefreshesExistingUsersOnlyAfterValidation() throws Exception {
        CustomScopeJdbcFixture fixture = new CustomScopeJdbcFixture(true);
        RoleEntity origin = new RoleEntity();
        origin.setDataScope("0003");
        origin.setDataOrgIds("12,13");
        origin.setFrozen(org.pkaq.core.enums.FrozenEnumm.FROZEN);
        when(fixture.roleMapper.selectById(5L)).thenReturn(origin);
        when(fixture.roleUserMapper.selectObjs(any())).thenReturn(List.of(10L));
        RoleAoeBo edit = bo(null);
        edit.setId(5L);
        edit.setFrozen(0);
        ThreadUserHelper.runWithUser(user("0007"), () -> fixture.service.editUniqueCode(edit));
        ArgumentCaptor<RoleEntity> saved = ArgumentCaptor.forClass(RoleEntity.class);
        verify(fixture.roleMapper).insertOrUpdate(saved.capture());
        assertEquals("12,13", saved.getValue().getDataOrgIds());
        assertEquals(origin.getFrozen(), saved.getValue().getFrozen());
        verify(fixture.userMapper).incrementPermVer(10L);
        verify(fixture.connection).commit();
    }

    @Test
    void invalidCustomEditRollsBackBeforeAnyRoleOrPermissionWrite() throws Exception {
        CustomScopeJdbcFixture fixture = new CustomScopeJdbcFixture(true);
        RoleEntity origin = new RoleEntity();
        origin.setDataScope("0003");
        origin.setDataOrgIds("12");
        when(fixture.roleMapper.selectById(5L)).thenReturn(origin);
        RoleAoeBo edit = bo("12,16");
        edit.setId(5L);
        ThreadUserHelper.runWithUser(user("0007"), () -> assertThrows(RuntimeException.class,
                () -> fixture.service.editUniqueCode(edit)));
        assertNoWrites(fixture);
        assertEquals("12", origin.getDataOrgIds());
        verify(fixture.connection).rollback();
    }

    private void reject(CustomScopeJdbcFixture fixture, ThreadUser user, String ids) throws Exception {
        assertThrows(RuntimeException.class, () -> run(fixture, user, ids));
        assertNoWrites(fixture);
        verify(fixture.connection).rollback();
        verify(fixture.connection, never()).commit();
    }

    private void assertNoWrites(CustomScopeJdbcFixture fixture) {
        verify(fixture.roleMapper, never()).insertOrUpdate(any(RoleEntity.class));
        verifyNoInteractions(fixture.roleUserMapper, fixture.roleResourceMapper, fixture.userMapper);
    }

    private void run(CustomScopeJdbcFixture fixture, ThreadUser user, String ids) {
        ThreadUserHelper.runWithUser(user, () -> fixture.service.editUniqueCode(bo(ids)));
    }

    private ThreadUser user(String scope) {
        return new ThreadUser().setUserId(7L).setTenantId(0L).setDataScopes(scope == null ? List.of()
                : List.of(new ThreadUser.DataScope(scope, List.of())));
    }

    private RoleAoeBo bo(String ids) {
        RoleAoeBo bo = new RoleAoeBo();
        bo.setCode("SYNTHETIC_CUSTOM");
        bo.setName("合成自定义角色");
        bo.setDataScope("0003");
        bo.setDataOrgIds(ids);
        return bo;
    }
}
