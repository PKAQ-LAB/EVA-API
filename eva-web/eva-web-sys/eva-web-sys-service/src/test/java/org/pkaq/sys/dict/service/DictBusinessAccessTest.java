package org.pkaq.sys.dict.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.cache.DictCacheHelper;
import org.pkaq.sys.dict.convert.DictConvert;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.mapper.DictItemMapper;
import org.pkaq.sys.dict.mapper.DictMapper;
import org.pkaq.sys.dict.vo.DictListResponse;
import org.pkaq.sys.dict.vo.DictViewVo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * standalone分类、实际维护关系、资源与响应能力隔离回归，无真实数据库。
 * @author PKAQ
 * @date 2026-10-31
 */
class DictBusinessAccessTest {
    private final EvaConfig config = new EvaConfig();
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final DataSource dataSource = mock(DataSource.class);
    private final DictBusinessAccess access = new DictBusinessAccess(config, jdbc,
            mock(org.pkaq.core.mybatis.tenant.CoreSchemaExecutor.class),
            mock(org.pkaq.core.mybatis.tenant.TenantSchemaResolver.class));
    private final DictMapper mapper = mock(DictMapper.class);
    private final DictItemMapper items = mock(DictItemMapper.class);
    private final DictItemSnapshot snapshot = mock(DictItemSnapshot.class);
    private final DictService service = new DictService(mock(DictCacheHelper.class), items,
            Mappers.getMapper(DictConvert.class), config, snapshot, access);
    private final ThreadUser user = new ThreadUser().setUserId(7L).setTenantId(0L);
    private boolean administrator = true;
    private boolean version = true;
    private boolean incomplete;

    @BeforeEach
    void initialize() {
        config.getResourcePermission().setEnable(false);
        ReflectionTestUtils.setField(service, "mapper", mapper);
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.bindResource(dataSource, new ConnectionHolder(mock(Connection.class)));
        when(jdbc.getDataSource()).thenReturn(dataSource);
        when(jdbc.queryForObject(anyString(), eq(String.class))).thenReturn("eva");
        when(jdbc.queryForObject(anyString(), eq(String.class), anyString())).thenReturn("eva");
        when(jdbc.queryForList(anyString(), eq(String.class), anyString(), anyString())).thenReturn(List.of(
                "id", "revision", "deleted", "frozen", "sort", "create_id", "create_by", "utc_create",
                "modify_id", "modify_by", "utc_modify", "remark", "type", "code", "name", "category",
                "main_id", "d_code", "d_value"));
        when(jdbc.queryForList(anyString(), eq(String.class))).thenReturn(
                List.of("CHECK ((category IS NULL) OR (category = ANY (ARRAY['SYSTEM','BUSINESS'])))"));
        when(jdbc.queryForObject(anyString(), eq(Boolean.class), eq(7L), eq("ROLE_ADMIN")))
                .thenAnswer(call -> administrator);
        when(jdbc.queryForList(anyString(), eq(Long.class), eq(7L))).thenReturn(List.of(10L));
        when(mapper.selectVisible(eq("eva"), any(Wrapper.class))).thenAnswer(call -> {
            var wrapper = (com.baomidou.mybatisplus.core.conditions.AbstractWrapper<?, ?, ?>) call.getArgument(1);
            wrapper.getSqlSegment();
            Long id = wrapper.getParamNameValuePairs().values().stream()
                    .filter(Long.class::isInstance).map(Long.class::cast).findFirst().orElse(null);
            DictEntity found = id == null ? null : mapper.selectById(id);
            return found != null && wrapper.getParamNameValuePairs().containsValue(found.getCategory())
                    ? List.of(found) : List.of();
        });
        when(mapper.insertPrivate(eq("eva"), any())).thenReturn(1);
        when(mapper.updatePrivate(eq("eva"), any(), anyInt())).thenReturn(1);
        when(jdbc.queryForObject(anyString(), eq(Boolean.class), eq("eva"))).thenAnswer(call -> version);
        when(jdbc.queryForObject(anyString(), eq(Boolean.class), eq(7L)))
                .thenAnswer(call -> ((String) call.getArgument(0)).contains("role.CODE") ? administrator : true);
        when(jdbc.queryForObject(anyString(), eq(Boolean.class))).thenAnswer(call -> incomplete);
        when(jdbc.queryForObject(anyString(), eq(Boolean.class), anyString(), nullable(Long.class),
                nullable(Long.class))).thenReturn(false);
    }

    @AfterEach
    void clear() {
        TransactionSynchronizationManager.unbindResource(dataSource);
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(false);
        TenantContext.clear();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void switchesAndForgedRoleNeverReplaceEffectiveAdministrator(boolean enabled) {
        administrator = false;
        config.getResourcePermission().setEnable(enabled);
        config.getDataPermission().setEnable(enabled);
        user.setRolesMap(Map.of(9L, new ThreadUser.GrantedRoles("伪资格", "ROLE_ADMIN")));
        user.setDataScopes(List.of(new ThreadUser.DataScope("0000", List.of())));
        ThreadUserHelper.runWithUser(user, () -> {
            assertFalse(access.businessWritable());
            assertThrows(RuntimeException.class, () -> access.requireWrite("POST", "/sys/dictionary/edit"));
        });
    }

    @Test
    void emptyClassifiedListCarriesFreshTrueOrFalseCapability() {
        when(mapper.selectVisible(eq("eva"), any(Wrapper.class))).thenReturn(List.of());
        ThreadUserHelper.runWithUser(user, () -> {
            assertTrue(service.listWithCapability().businessWritable());
            assertTrue(service.listWithCapability().data().isEmpty());
            administrator = false;
            assertFalse(service.listWithCapability().businessWritable());
            incomplete = true;
            assertThrows(IllegalStateException.class, service::listWithCapability);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM", "BUSINESS", "unclassified"})
    void rowAndGetReadOnlyFollowActualCategory(String category) {
        DictEntity main = main(1L, "unclassified".equals(category) ? null : category);
        main.setRemark("数据库备注");
        when(mapper.selectById(1L)).thenReturn(main);
        when(items.selectVisible(eq("eva"), any(Wrapper.class))).thenReturn(List.of());
        ThreadUserHelper.runWithUser(user, () -> {
            DictAoeBo input = bo();
            if ("unclassified".equals(category)) {
                assertThrows(RuntimeException.class, () -> service.getDict(input));
                return;
            }
            DictViewVo view = service.getDict(input);
            assertEquals(main.getCategory(), view.getCategory());
            assertEquals("数据库备注", view.getRemark());
            assertEquals(!"BUSINESS".equals(category), view.isReadOnly());
            input.setCategory("BUSINESS".equals(category) ? "SYSTEM" : "BUSINESS");
            assertThrows(RuntimeException.class, () -> service.getDict(input));
            administrator = false;
            input.setCategory(null);
            assertTrue(service.getDict(input).isReadOnly());
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM", "unclassified"})
    void allWriteEntrypointsRejectSystemOrUnclassifiedEvenWhenUnfrozen(String category) {
        DictEntity main = main(1L, "unclassified".equals(category) ? null : category);
        when(snapshot.load(1L)).thenReturn(new DictItemSnapshot.Snapshot(main, List.of()));
        when(mapper.selectById(1L)).thenReturn(main);
        SingleArray<Long> ids = new SingleArray<>();
        ids.setParam(Set.of(1L));
        ThreadUserHelper.runWithUser(user, () -> {
            assertThrows(RuntimeException.class, () -> service.edit(bo()));
            assertThrows(RuntimeException.class, () -> service.delDict(1L, "GET"));
            assertThrows(RuntimeException.class, () -> service.delDict(1L));
            assertThrows(RuntimeException.class, () -> service.switchFrozen(ids));
        });
        verify(mapper, never()).updatePrivate(eq("eva"), any(DictEntity.class), anyInt());
        verify(mapper, never()).deletePrivate(eq("eva"), any(DictEntity.class), anyInt());
        verify(items, never()).delete(any(Wrapper.class));
    }

    @Test
    void createForcesBusinessAndRejectsSystemInputWithoutWrite() {
        ThreadUserHelper.runWithUser(user, () -> {
            DictAoeBo input = bo();
            input.setId(null);
            service.edit(input);
            org.mockito.ArgumentCaptor<DictEntity> created = org.mockito.ArgumentCaptor.forClass(DictEntity.class);
            verify(mapper).insertPrivate(eq("eva"), created.capture());
            assertEquals("BUSINESS", created.getValue().getCategory());
            org.mockito.Mockito.clearInvocations(mapper);
            input.setCategory("SYSTEM");
            assertThrows(RuntimeException.class, () -> service.edit(input));
            verify(mapper, never()).insertPrivate(eq("eva"), any(DictEntity.class));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"system", "UNKNOWN", ""})
    void categoryEnumIsStrict(String category) {
        assertThrows(RuntimeException.class, () -> access.validateCategory(category));
    }

    @Test
    void sourceVersionContextAndMissingIdentityFailClosed() {
        assertThrows(RuntimeException.class, access::businessWritable);
        ThreadUserHelper.runWithUser(user, () -> {
            version = false;
            assertThrows(IllegalStateException.class, access::businessWritable);
            version = true;
            TenantContext.bind(0L, "other_schema");
            assertThrows(RuntimeException.class, access::businessWritable);
            TenantContext.clear();
            config.setMode("saas");
            assertThrows(RuntimeException.class, access::businessWritable);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST:/sys/dictionary/edit", "*:/sys/dictionary", "*:/sys/dictionary/"})
    void effectiveOperationResourceMatchesExistingSemantics(String grant) {
        config.getResourcePermission().setEnable(true);
        String[] parts = grant.split(":", 2);
        when(jdbc.queryForList(anyString())).thenAnswer(call -> {
            String sql = call.getArgument(0);
            assertTrue(sql.contains("JOIN SYS_MODULE module"));
            assertTrue(sql.contains("module.FROZEN IN (0, 9999)"));
            return List.of(Map.of("resource_type", parts[0], "resource_url", parts[1]));
        });
        ThreadUserHelper.runWithUser(user, () -> {
            access.requireWrite("POST", "/sys/dictionary/edit");
            assertThrows(RuntimeException.class, () -> access.requireWrite("POST", "/sys/dictionaryOther/edit"));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"public", "missing"})
    void foreignOrMissingItemRelationRejectsManagementReadBeforeMapper(String relation) {
        Map<String, Object> source = new java.util.HashMap<>();
        source.put("current_schema_name", "eva");
        source.put("relation_schema_name", "missing".equals(relation) ? null : relation);
        when(jdbc.queryForObject(anyString(), eq(String.class), eq("sys_dict_item")))
                .thenReturn("missing".equals(relation) ? null : relation);
        ThreadUserHelper.runWithUser(user, () -> {
            assertThrows(RuntimeException.class, access::businessWritable);
            assertThrows(RuntimeException.class, service::listWithCapability);
            assertThrows(RuntimeException.class, () -> service.getDict(bo()));
        });
        org.mockito.Mockito.verifyNoInteractions(mapper, items);
    }

    @Test
    void businessUpdateWithoutCategoryPreservesClassificationAndStoppedRowsRemainEditable() {
        DictEntity main = main(1L, "BUSINESS");
        main.setFrozen(FrozenEnumm.FROZEN);
        when(snapshot.load(1L)).thenReturn(new DictItemSnapshot.Snapshot(main, List.of()));
        when(items.selectList(any(Wrapper.class))).thenReturn(List.of());
        ThreadUserHelper.runWithUser(user, () -> {
            assertFalse(access.readOnly(main, true));
            service.edit(bo());
            org.mockito.ArgumentCaptor<DictEntity> updated = org.mockito.ArgumentCaptor.forClass(DictEntity.class);
            verify(mapper).updatePrivate(eq("eva"), updated.capture(), anyInt());
            assertEquals("BUSINESS", updated.getValue().getCategory());
            main.setFrozen(FrozenEnumm.READ_ONLY);
            assertTrue(access.readOnly(main, true));
            assertThrows(RuntimeException.class, () -> service.edit(bo()));
        });
    }

    @Test
    void mixedCategorySwitchValidatesWholeBatchBeforeWrite() {
        when(mapper.selectVisible(eq("eva"), any(Wrapper.class))).thenAnswer(call -> {
            var wrapper = (com.baomidou.mybatisplus.core.conditions.AbstractWrapper<?, ?, ?>) call.getArgument(1);
            wrapper.getSqlSegment();
            boolean first = wrapper.getParamNameValuePairs().containsValue(1L);
            return first ? List.of(main(1L, "BUSINESS")) : List.of();
        });
        SingleArray<Long> ids = new SingleArray<>();
        ids.setParam(Set.of(1L, 2L));
        ThreadUserHelper.runWithUser(user, () ->
                assertThrows(RuntimeException.class, () -> service.switchFrozen(ids)));
        verify(mapper, never()).updatePrivate(eq("eva"), any(DictEntity.class), anyInt());
    }

    @Test
    void globalReservedCodeCannotBeHiddenByPersonalDataScope() {
        when(jdbc.queryForObject(anyString(), eq(Boolean.class), anyString(), eq("status"), nullable(Long.class),
                nullable(Long.class))).thenReturn(true);
        ThreadUserHelper.runWithUser(user, () ->
                assertThrows(RuntimeException.class, () -> service.edit(bo())));
        org.mockito.Mockito.verifyNoInteractions(snapshot, items);
        verify(mapper, never()).insertPrivate(eq("eva"), any(DictEntity.class));
        verify(mapper, never()).updatePrivate(eq("eva"), any(DictEntity.class), anyInt());
    }

    @Test
    void effectiveAdministratorStillNeedsAnOperationGrantWhenEnabled() {
        config.getResourcePermission().setEnable(true);
        when(jdbc.queryForList(anyString(), eq(7L))).thenReturn(List.of());
        ThreadUserHelper.runWithUser(user, () -> {
            assertTrue(access.businessWritable());
            assertThrows(RuntimeException.class, () -> service.edit(bo()));
            assertThrows(RuntimeException.class, () -> service.delDict(1L));
            assertThrows(RuntimeException.class, () -> service.delDict(1L, "GET"));
            assertThrows(RuntimeException.class, () -> service.switchFrozen(new SingleArray<>()));
        });
        org.mockito.Mockito.verifyNoInteractions(mapper, items, snapshot);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET:/sys/dictionary/edit", "*:/"})
    void wrongMethodAndRootWildcardNeverGrantDescendantWrite(String grant) {
        config.getResourcePermission().setEnable(true);
        String[] parts = grant.split(":", 2);
        when(jdbc.queryForList(anyString(), eq(7L))).thenReturn(
                List.of(Map.of("resource_type", parts[0], "resource_url", parts[1])));
        ThreadUserHelper.runWithUser(user, () ->
                assertThrows(RuntimeException.class, () -> access.requireWrite("POST", "/sys/dictionary/edit")));
    }

    @Test
    void falseBooleansRemainNativeAndExplicitUnderNonDefaultSerialization() throws Exception {
        ObjectMapper json = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        DictViewVo row = new DictViewVo();
        row.setCategory("BUSINESS");
        row.setReadOnly(false);
        var tree = json.readTree(json.writeValueAsString(DictListResponse.fromSuccess(
                Response.success(List.of(row)), false)));
        assertTrue(tree.get("businessWritable").isBoolean());
        assertFalse(tree.get("businessWritable").booleanValue());
        assertTrue(tree.get("data").get(0).get("readOnly").isBoolean());
        assertFalse(tree.get("data").get(0).get("readOnly").booleanValue());
        assertThrows(NoSuchFieldException.class, () -> DictAoeBo.class.getDeclaredField("businessWritable"));
        assertThrows(NoSuchFieldException.class, () -> DictAoeBo.class.getDeclaredField("readOnly"));
    }

    private DictEntity main(long id, String category) {
        DictEntity entity = new DictEntity();
        entity.setId(id);
        entity.setCategory(category);
        entity.setCode("status");
        entity.setFrozen(FrozenEnumm.UN_FROZEN);
        return entity;
    }

    private DictAoeBo bo() {
        DictAoeBo input = new DictAoeBo();
        input.setId(1L);
        input.setCode("status");
        input.setName("状态");
        return input;
    }
}
