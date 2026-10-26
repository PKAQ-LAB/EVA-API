package org.pkaq.sys.role.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.json.JacksonObjectMapper;
import org.pkaq.sys.user.vo.UserDetailVo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 已授角色详情真实Service/MapStruct/Mapper/XML/插件及只读事务回归，无实际数据库。
 * @author PKAQ
 * @date 2026-10-24
 */
class AssignedRoleDetailTest {
    @AfterEach
    void cleanup() {
        TenantContext.clear();
        assertNull(ThreadUserHelper.getCurrentUserOrNull());
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertTrue(TransactionSynchronizationManager.getResourceMap().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void emptyReferencesNeverQueryCandidatesAndKeepDepartmentAndPosts(boolean dataEnabled) throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(dataEnabled);
        fixture.posts.add(9L);
        UserDetailVo detail = read(fixture, user("0000"));
        assertEquals(fixture.target, detail.getId());
        assertEquals(42L, detail.getDeptId());
        assertEquals(List.of(9L), detail.getPostId());
        assertEquals(List.of(), detail.getRoleIds());
        assertEquals(List.of(), detail.getAssignedRoles());
        assertEquals(1, fixture.referenceReads);
        assertEquals(0, fixture.roleQueries);
        noWrites(fixture);
    }

    @Test
    void singleExactAssignmentDoesNotIncludeAnotherUsersRoleOrAnyCandidate() throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(true);
        fixture.assigned.add(12L);
        fixture.role("eva", 12L, "真实合成角色", "mixedCaseRaw", 0, 0L, 7L);
        fixture.role("eva", 99L, "不属于目标", "ROLE_OTHER", 0, 0L, 7L);
        UserDetailVo detail = read(fixture, user("0000"));
        assertEquals(List.of(12L), detail.getRoleIds());
        assertEquals(1, detail.getAssignedRoles().size());
        assertEquals("mixedCaseRaw", detail.getAssignedRoles().getFirst().getCode());
        assertEquals("真实合成角色", detail.getAssignedRoles().getFirst().getName());
        assertEquals(1, fixture.referenceReads);
        assertEquals(1, fixture.roleQueries);
        String query = roleSql(fixture).toLowerCase();
        assertTrue(query.contains("id in (?)"), query);
        assertFalse(query.contains("limit"));
        assertFalse(query.contains("tenant_id"));
        assertTrue(query.contains("deleted = 0") || query.contains("deleted=0"));
        assertTrue(query.contains("1 = 1"), query);
        noWrites(fixture);
    }

    @Test
    void moreThanTwoHundredAssignmentsAreReadOnceAndLongIdsStayExact() throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(true);
        long first = 2113894345662021700L;
        for (int index = 0; index < 207; index++) {
            long id = first + index;
            fixture.assigned.add(id);
            fixture.role("eva", id, "合成" + index, "raw" + index, 0, 0L, 7L);
        }
        fixture.assigned.add(first);
        UserDetailVo detail = read(fixture, user("0000"));
        assertEquals(fixture.assigned, detail.getRoleIds());
        assertEquals(207, detail.getAssignedRoles().size());
        assertEquals(207, detail.getAssignedRoles().stream().map(role -> role.getId()).distinct().count());
        assertEquals(1, fixture.referenceReads);
        assertEquals(1, fixture.roleQueries);
        assertTrue(new JacksonObjectMapper().writeValueAsString(detail).contains("\"" + first + "\""));
        noWrites(fixture);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 9999})
    void assignedFrozenAndReadOnlyRolesAndReadOnlyAccountsRemainViewable(int state) throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(true);
        fixture.accountFrozen = 9999;
        fixture.assigned.add(12L);
        fixture.role("eva", 12L, "状态", "raw", state, 0L, 7L);
        UserDetailVo detail = read(fixture, user("0000"));
        assertEquals(state, detail.getAssignedRoles().getFirst().getFrozen());
        noWrites(fixture);
    }

    @Test
    void rawNullMetadataStaysNullWithoutEntityDefaults() throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(true);
        fixture.assigned.add(12L);
        fixture.role("eva", 12L, null, null, null, 0L, 7L);
        var summary = read(fixture, user("0000")).getAssignedRoles().getFirst();
        assertEquals(12L, summary.getId());
        assertNull(summary.getName());
        assertNull(summary.getCode());
        assertNull(summary.getFrozen());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void missingDeletedAndHiddenRolesOnlyOmitSummariesNeverOriginalIds(boolean dataEnabled) throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(dataEnabled);
        fixture.assigned.addAll(Arrays.asList(12L, 13L, 14L, 777L, 12L, null, -1L));
        fixture.role("eva", 12L, "自己", "raw12", 1, 0L, 7L);
        fixture.role("eva", 13L, "隐藏", "raw13", 9999, 0L, 88L);
        fixture.role("eva", 14L, "已删", "raw14", 0, 1L, 7L);
        UserDetailVo detail = read(fixture, user("0007"));
        assertEquals(fixture.assigned, detail.getRoleIds());
        assertEquals(dataEnabled ? List.of(12L) : List.of(12L, 13L),
                detail.getAssignedRoles().stream().map(role -> role.getId()).toList());
        if (dataEnabled) {
            assertTrue(roleSql(fixture).toLowerCase().contains("create_id = 7"));
        }
        noWrites(fixture);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void missingOrDataInvisibleTargetNeverReadsRelationships(boolean missing) throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(true);
        fixture.accountPresent = !missing;
        fixture.accountCreator = 88L;
        reject(fixture, user("0007"));
        assertEquals(1, fixture.accountQueries);
        assertEquals(0, fixture.referenceReads);
        assertEquals(0, fixture.roleQueries);
    }

    @ParameterizedTest
    @ValueSource(strings = {"sys_account", "sys_account_profile", "sys_roleuser_ref", "sys_role",
            "sys_organization", "sys_postuser_ref"})
    void anyRequiredTableResolvingToCoreFailsBeforeAccountRead(String table) throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(false);
        fixture.namespaces.put(table, "eva_core");
        reject(fixture, user("0000"));
        assertEquals(0, fixture.accountQueries);
        assertEquals(0, fixture.referenceReads);
    }

    @Test
    void missingTableThirdSchemaAndWrongCurrentSchemaFailClosed() throws Exception {
        for (String schema : Arrays.asList(null, "public", "third")) {
            AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(false);
            fixture.namespaces.put("sys_role", schema);
            reject(fixture, user("0000"));
            assertEquals(0, fixture.accountQueries);
        }
        AssignedRoleDetailJdbcFixture wrong = new AssignedRoleDetailJdbcFixture(false);
        wrong.currentSchema = "third";
        reject(wrong, user("0000"));
        assertEquals(0, wrong.accountQueries);
    }

    @Test
    void identityModeAndContextConflictsRemainGuardsWithDataDisabled() throws Exception {
        for (long id : List.of(0L, -1L)) {
            AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(false);
            reject(fixture, user("0000").setUserId(id));
            assertTrue(fixture.sql.isEmpty());
        }
        AssignedRoleDetailJdbcFixture tenant = new AssignedRoleDetailJdbcFixture(false);
        reject(tenant, user("0000").setTenantId(22L));
        for (String mode : List.of("unknown", "platform", "saas")) {
            AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(false);
            fixture.config.setMode(mode);
            reject(fixture, user("0000"));
        }
        AssignedRoleDetailJdbcFixture mixed = new AssignedRoleDetailJdbcFixture(false);
        mixed.config.getTenant().setEnable(true);
        mixed.config.getTenant().setMode("schema");
        reject(mixed, user("0000"));
        for (String schema : Arrays.asList("third", null)) {
            TenantContext.bind(0L, schema);
            AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(false);
            reject(fixture, user("0000"));
            assertEquals(schema, TenantContext.schemaName());
        }
    }

    @Test
    void unauthenticatedAndUnboundOrAbsentTransactionsCannotCallMetadataHelper() throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(false);
        assertThrows(RuntimeException.class, () -> fixture.service.getUser(fixture.target));
        assertTrue(fixture.sql.isEmpty());
        ThreadUserHelper.runWithUser(user("0000"), () -> assertThrows(RuntimeException.class,
                () -> fixture.roleReads.readUserRoles(fixture.target)));
        AssignedRoleDetailJdbcFixture other = new AssignedRoleDetailJdbcFixture(false);
        ReflectionTestUtils.setField(other.roleReads, "jdbcTemplate", new JdbcTemplate(mock(DataSource.class)));
        reject(other, user("0000"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Eva", "eva.other", "bad;schema"})
    void invalidStandaloneSchemaCannotReadAccount(String schema) throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(false);
        fixture.config.getTenant().setStandaloneSchema(schema);
        reject(fixture, user("0000"));
        assertEquals(0, fixture.accountQueries);
    }

    @Test
    void singletonAndMatchingStandaloneContextWorkWithoutChangingContext() throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(true);
        fixture.config.setMode("singleton");
        TenantContext.bind(0L, "eva");
        assertEquals(List.of(), read(fixture, user("0000")).getAssignedRoles());
        assertEquals(0L, TenantContext.tenantId());
        assertEquals("eva", TenantContext.schemaName());
    }

    @Test
    void platformZeroAndMissingPositiveContextCannotUseSchemaMode() throws Exception {
        AssignedRoleDetailJdbcFixture platform = new AssignedRoleDetailJdbcFixture(false);
        platform.config.setMode("platform");
        platform.config.getTenant().setEnable(true);
        platform.config.getTenant().setMode("schema");
        reject(platform, user("0000"));
        assertTrue(platform.sql.isEmpty());
        AssignedRoleDetailJdbcFixture missing = tenantFixture(22L);
        TenantContext.clear();
        reject(missing, user("0000").setTenantId(22L));
        assertTrue(missing.sql.isEmpty());
        TenantContext.bind(null, "eva");
        AssignedRoleDetailJdbcFixture partial = new AssignedRoleDetailJdbcFixture(false);
        reject(partial, user("0000"));
        assertEquals("eva", TenantContext.schemaName());
    }

    @Test
    void unknownStoredStateFailsInsteadOfInventingNormalOrEmptyMetadata() throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(true);
        fixture.assigned.add(12L);
        fixture.role("eva", 12L, "未知合成状态", "raw", 2, 0L, 7L);
        reject(fixture, user("0000"));
        assertEquals(1, fixture.roleQueries);
    }

    @ParameterizedTest
    @ValueSource(longs = {22L, 23L})
    void positiveTenantUsesAuthoritativeMappingAndSameIdNeverReadsAnotherSchema(long tenant) throws Exception {
        AssignedRoleDetailJdbcFixture fixture = tenantFixture(tenant);
        fixture.assigned.add(12L);
        fixture.role("tenant_a", 12L, "A", "aRaw", 0, 0L, 7L);
        fixture.role("tenant_b", 12L, "B", "bRaw", 0, 0L, 7L);
        String schema = tenant == 22L ? "tenant_a" : "tenant_b";
        String path = fixture.searchPath;
        UserDetailVo detail = read(fixture, user("0000").setTenantId(tenant));
        assertEquals(tenant == 22L ? "A" : "B", detail.getAssignedRoles().getFirst().getName());
        assertEquals(List.of("eva_core", path), fixture.schemaTransitions);
        assertEquals(path, fixture.searchPath);
        assertEquals(schema, TenantContext.schemaName());
        assertEquals(tenant, TenantContext.tenantId());
        assertFalse(roleSql(fixture).toLowerCase().contains("tenant_id"));
        noWrites(fixture);
    }

    @Test
    void positiveContextOrRelationMismatchAndMappingFailurePreserveOriginalContext() throws Exception {
        AssignedRoleDetailJdbcFixture fixture = tenantFixture(22L);
        fixture.namespaces.put("sys_roleuser_ref", "tenant_b");
        reject(fixture, user("0000").setTenantId(22L));
        assertEquals("tenant_a,eva_core", fixture.searchPath);
        assertEquals(0, fixture.accountQueries);
        AssignedRoleDetailJdbcFixture mismatch = tenantFixture(22L);
        TenantContext.bind(22L, "tenant_b");
        reject(mismatch, user("0000").setTenantId(22L));
        assertEquals("tenant_b", TenantContext.schemaName());
        AssignedRoleDetailJdbcFixture absent = tenantFixture(24L);
        reject(absent, user("0000").setTenantId(24L));
        assertEquals("tenant_b,eva_core", absent.searchPath);
    }

    @Test
    void detailPostsNeverReenterRoutingOrUseTenantShadowOfControlTable() throws Exception {
        AssignedRoleDetailJdbcFixture fixture = tenantFixture(22L);
        fixture.tenantControlShadow = true;
        fixture.postsBySchema.put("tenant_a", List.of(42L, 9L, 42L));
        fixture.postsBySchema.put("tenant_b", List.of(999L));
        UserDetailVo detail = read(fixture, user("0000").setTenantId(22L));
        assertEquals(List.of(42L, 9L, 42L), detail.getPostId());
        assertEquals(1, fixture.controlPlaneLookups);
        assertEquals(0, fixture.shadowLookups);
        assertEquals(List.of("eva_core", "tenant_a,eva_core"), fixture.schemaTransitions);
        assertTrue(fixture.queryOrigins.contains("sys_postuser_ref@tenant_a"));
        assertFalse(fixture.queryOrigins.stream().anyMatch(origin -> origin.endsWith("@tenant_b")));
        noWrites(fixture);
    }

    @Test
    void invalidNullPostEntityKeepsOriginalFailureRatherThanInventingEmptyPosts() throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(true);
        fixture.posts.add(null);
        reject(fixture, user("0000"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void actualOuterTenantAspectUsesTrustedMappingAndRestoresOnSuccessOrFailure(boolean failure) throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(true, true);
        fixture.config.setMode("saas");
        fixture.config.getTenant().setEnable(true);
        fixture.config.getTenant().setMode("schema");
        fixture.namespaces.replaceAll((table, schema) -> "tenant_a");
        fixture.currentSchema = "eva_core";
        fixture.searchPath = "eva_core";
        fixture.postsBySchema.put("tenant_a", List.of(42L));
        fixture.tenantControlShadow = true;
        if (failure) {
            fixture.namespaces.put("sys_role", "eva_core");
            reject(fixture, user("0000").setTenantId(22L));
            assertEquals(0, fixture.accountQueries);
        } else {
            assertEquals(List.of(42L), read(fixture, user("0000").setTenantId(22L)).getPostId());
            assertTrue(fixture.queryOrigins.contains("sys_postuser_ref@tenant_a"));
        }
        assertEquals(2, fixture.controlPlaneLookups);
        assertEquals(0, fixture.shadowLookups);
        assertEquals(List.of("tenant_a,eva_core", "eva_core", "tenant_a,eva_core", "eva_core"),
                fixture.schemaTransitions);
        assertEquals("eva_core", fixture.searchPath);
        assertNull(TenantContext.tenantId());
        assertNull(TenantContext.schemaName());
        noWrites(fixture);
    }

    @Test
    void missingOrShadowedControlTableCannotSupplyAuthoritativeMapping() throws Exception {
        for (String namespace : Arrays.asList(null, "pg_temp_9", "third")) {
            AssignedRoleDetailJdbcFixture fixture = tenantFixture(22L);
            fixture.controlNamespace = namespace;
            reject(fixture, user("0000").setTenantId(22L));
            assertEquals(0, fixture.controlPlaneLookups);
            assertEquals(0, fixture.accountQueries);
            assertEquals("tenant_a,eva_core", fixture.searchPath);
            assertEquals("tenant_a", TenantContext.schemaName());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"catalog", "sys_account", "sys_roleuser_ref", "sys_role", "sys_postuser_ref"})
    void actualSqlExceptionsFailWholeRequestAndNeverWrite(String failure) throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(true);
        fixture.assigned.add(12L);
        fixture.catalogFailure = "catalog".equals(failure);
        fixture.failingTable = failure;
        TenantContext.bind(0L, "eva");
        reject(fixture, user("0000"));
        assertEquals("eva", TenantContext.schemaName());
    }

    private AssignedRoleDetailJdbcFixture tenantFixture(long tenant) throws Exception {
        AssignedRoleDetailJdbcFixture fixture = new AssignedRoleDetailJdbcFixture(true);
        fixture.config.setMode("saas");
        fixture.config.getTenant().setEnable(true);
        fixture.config.getTenant().setMode("schema");
        String schema = tenant == 22L ? "tenant_a" : "tenant_b";
        fixture.source(schema);
        TenantContext.bind(tenant, schema);
        return fixture;
    }

    private UserDetailVo read(AssignedRoleDetailJdbcFixture fixture, ThreadUser user) {
        AtomicReference<UserDetailVo> result = new AtomicReference<>();
        ThreadUserHelper.runWithUser(user, () -> result.set(fixture.service.getUser(fixture.target)));
        return result.get();
    }

    private void reject(AssignedRoleDetailJdbcFixture fixture, ThreadUser user) throws Exception {
        assertThrows(RuntimeException.class, () -> read(fixture, user));
        verify(fixture.connection).rollback();
        noWrites(fixture);
    }

    private void noWrites(AssignedRoleDetailJdbcFixture fixture) {
        assertEquals(0, fixture.writes);
        verifyNoInteractions(fixture.profiles, fixture.accountCreation);
    }

    private String roleSql(AssignedRoleDetailJdbcFixture fixture) {
        return fixture.sql.stream().filter(query -> query.toLowerCase().contains("from sys_role "))
                .findFirst().orElseThrow();
    }

    private ThreadUser user(String scope) {
        return new ThreadUser().setUserId(7L).setTenantId(0L)
                .setDataScopes(List.of(new ThreadUser.DataScope(scope, List.of())));
    }
}
