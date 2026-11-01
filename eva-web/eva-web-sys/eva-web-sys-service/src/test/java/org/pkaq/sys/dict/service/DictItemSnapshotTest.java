package org.pkaq.sys.dict.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.pkaq.config.MybatisPlusDataPermissionHandler;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.mybatis.tenant.TenantSchemaResolver;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.bo.DictLineBo;
import org.pkaq.sys.dict.cache.DictCacheHelper;
import org.pkaq.sys.dict.convert.DictConvert;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.pkaq.sys.dict.mapper.DictItemMapper;
import org.pkaq.sys.dict.mapper.DictMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 真实数据权限插件与内部固定JDBC保护读取的合成回归，不连接开发库。
 * @author PKAQ
 * @date 2026-10-31
 */
class DictItemSnapshotTest {
    private final EvaConfig config = new EvaConfig();
    private final DictMapper mapper = mock(DictMapper.class);
    private final DictItemMapper items = mock(DictItemMapper.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final DataSource dataSource = mock(DataSource.class);
    private final CoreSchemaExecutor core = mock(CoreSchemaExecutor.class);
    private final TenantSchemaResolver resolver = mock(TenantSchemaResolver.class);
    private final DictItemSnapshot snapshot = new DictItemSnapshot(mapper, jdbc, config, core, resolver);
    private final ThreadUser user = new ThreadUser().setUserId(7L).setTenantId(0L)
            .setDataScopes(List.of(new ThreadUser.DataScope("0007", List.of())));
    private final DictBusinessAccess access = mock(DictBusinessAccess.class);
    private final DictService service = new DictService(mock(DictCacheHelper.class), items,
            Mappers.getMapper(DictConvert.class), config, snapshot, access);

    @BeforeEach
    @SuppressWarnings("unchecked")
    void initialize() throws Exception {
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                new org.apache.ibatis.builder.MapperBuilderAssistant(new MybatisConfiguration(), "snapshot"),
                DictEntity.class);
        config.getDataPermission().setEnable(true);
        ReflectionTestUtils.setField(service, "mapper", mapper);
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.bindResource(dataSource, new ConnectionHolder(mock(Connection.class)));
        when(jdbc.getDataSource()).thenReturn(dataSource);
        when(jdbc.queryForMap(anyString(), anyString())).thenReturn(Map.of(
                "current_schema_name", "eva", "relation_schema_name", "eva"));
        DictEntity main = new DictEntity();
        main.setId(1L);
        main.setCode("status");
        main.setCategory("BUSINESS");
        main.setFrozen(FrozenEnumm.UN_FROZEN);
        when(mapper.selectVisible(anyString(), any(Wrapper.class))).thenAnswer(invocation -> {
            var wrapper = (com.baomidou.mybatisplus.core.conditions.AbstractWrapper<?, ?, ?>) invocation.getArgument(1);
            wrapper.getSqlSegment();
            return wrapper.getParamNameValuePairs().containsValue(2L) ? List.of() : List.of(main);
        });
        when(access.businessOwner()).thenReturn(new DictCacheHelper.Owner("eva", "BUSINESS"));
        when(access.sources()).thenReturn(List.of(new DictCacheHelper.Owner("eva", "BUSINESS")));
        when(mapper.updatePrivate(eq("eva"), any(), anyInt())).thenReturn(1);
        when(mapper.selectById(1L)).thenReturn(main);
        when(items.selectVisible(anyString(), any(Wrapper.class))).thenReturn(List.of());
        when(jdbc.query(anyString(), any(RowMapper.class), eq(1L))).thenAnswer(invocation -> {
            assertEquals(1L, (Long) invocation.getArgument(2));
            String sql = invocation.getArgument(0);
            assertTrue(sql.contains("MAIN_ID = ? AND DELETED = 0 AND FROZEN = 9999"));
            assertTrue(sql.contains("FOR UPDATE"));
            assertFalse(sql.contains("CREATE_ID"));
            ResultSet row = mock(ResultSet.class);
            when(row.getLong("ID")).thenReturn(11L);
            when(row.getLong("MAIN_ID")).thenReturn(1L);
            when(row.getString("D_CODE")).thenReturn("fixed");
            when(row.getString("D_VALUE")).thenReturn("固定");
            when(row.getDouble("SORT")).thenReturn(7D);
            when(row.getObject("FROZEN")).thenReturn(9999);
            return List.of(((RowMapper<DictItemEntity>) invocation.getArgument(1)).mapRow(row, 0));
        });
    }

    @AfterEach
    void clear() {
        TransactionSynchronizationManager.unbindResource(dataSource);
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(false);
        TenantContext.clear();
    }

    @Test
    void realPluginCutsOtherOwnerButIntegrityReadStillRejectsOmissionAndDelete() {
        ThreadUserHelper.runWithUser(user, () -> {
            MybatisConfiguration configuration = new MybatisConfiguration();
            MappedStatement statement = new MappedStatement.Builder(configuration,
                    DictItemMapper.class.getName() + ".selectList",
                    new StaticSqlSource(configuration, "SELECT * FROM SYS_DICT_ITEM WHERE MAIN_ID = 1"),
                    SqlCommandType.SELECT).build();
            var sql = statement.getBoundSql(null);
            try {
                new DataPermissionInterceptor(new MybatisPlusDataPermissionHandler(config))
                        .beforeQuery(mock(Executor.class), statement, null, RowBounds.DEFAULT, null, sql);
            } catch (Exception exception) {
                throw new AssertionError(exception);
            }
            assertTrue(sql.getSql().contains("CREATE_ID = 7"));
            assertTrue(sql.getSql().contains("MODIFY_ID = 7"));
            // 合成可见结果为空模拟作者99的保护行被SELF过滤，JDBC仍取回本主9999行。
            assertTrue(service.getDict(bo()).getLines().isEmpty());
            assertThrows(RuntimeException.class, () -> service.edit(bo()));
            assertThrows(RuntimeException.class, () -> service.delDict(1L));
            verify(mapper, never()).updatePrivate(eq("eva"), any(DictEntity.class), anyInt());
            verify(mapper, never()).deletePrivate(eq("eva"), any(DictEntity.class), anyInt());
            verify(items, never()).delete(any(Wrapper.class));
        });
    }

    @Test
    void unchangedProtectionAllowsMainWriteButHiddenOrdinaryIdDoesNot() {
        ThreadUserHelper.runWithUser(user, () -> {
            DictAoeBo input = bo();
            DictLineBo fixed = new DictLineBo();
            fixed.setId(11L);
            fixed.setKeyName("fixed");
            fixed.setKeyValue("固定");
            input.setLines(List.of(fixed));
            service.edit(input);
            verify(mapper).updatePrivate(eq("eva"), any(DictEntity.class), anyInt());
            verify(items, never()).updatePrivate(eq("eva"), any(DictItemEntity.class), anyInt());
            clearInvocations(mapper);
            fixed.setId(12L);
            assertThrows(RuntimeException.class, () -> service.edit(input));
            verify(mapper, never()).updatePrivate(eq("eva"), any(DictEntity.class), anyInt());
        });
    }

    @Test
    void noParentAuthorizationOrWrongSourceNeverReadsProtectionRows() {
        when(mapper.selectVisible(anyString(), any(Wrapper.class))).thenReturn(List.of());
        ThreadUserHelper.runWithUser(user, () -> assertThrows(RuntimeException.class, () -> snapshot.load(2L)));
        verify(jdbc, never()).query(anyString(), any(RowMapper.class), any(Long.class));
        when(jdbc.queryForMap(anyString(), anyString())).thenReturn(Map.of(
                "current_schema_name", "eva", "relation_schema_name", "other_schema"));
        ThreadUserHelper.runWithUser(user, () -> assertThrows(RuntimeException.class, () -> snapshot.load(1L)));
        verify(jdbc, never()).query(anyString(), any(RowMapper.class), any(Long.class));
    }

    @Test
    void noIdentityTransactionOrConflictingSchemaIsRejected() {
        assertThrows(RuntimeException.class, () -> snapshot.load(1L));
        TransactionSynchronizationManager.setActualTransactionActive(false);
        ThreadUserHelper.runWithUser(user, () -> assertThrows(RuntimeException.class, () -> snapshot.load(1L)));
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TenantContext.bind(0L, "other_schema");
        ThreadUserHelper.runWithUser(user, () -> assertThrows(RuntimeException.class, () -> snapshot.load(1L)));
        verify(jdbc, never()).query(anyString(), any(RowMapper.class), any(Long.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void privateSchemaMustMatchTrustedMappingBeforeAnyParentOrProtectionRead() {
        config.setMode("saas");
        config.getTenant().setEnable(true);
        config.getTenant().setMode("schema");
        user.setTenantId(23L);
        TenantContext.bind(23L, "tenant_expected");
        when(core.execute(any())).thenAnswer(invocation ->
                ((java.util.function.Function<JdbcTemplate, Object>) invocation.getArgument(0)).apply(jdbc));
        when(jdbc.queryForMap(anyString(), eq("sys_tenant"))).thenReturn(Map.of(
                "current_schema_name", "eva_core", "relation_schema_name", "eva_core"));
        when(resolver.resolve(23L)).thenReturn("tenant_other");
        ThreadUserHelper.runWithUser(user, () -> assertThrows(RuntimeException.class, () -> snapshot.load(1L)));
        verify(mapper, never()).selectVisible(anyString(), any(Wrapper.class));
        verify(jdbc, never()).query(anyString(), any(RowMapper.class), any(Long.class));
        assertEquals(23L, TenantContext.tenantId());
        assertEquals("tenant_expected", TenantContext.schemaName());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"tenant_expected", "tenant_other", "missing"})
    void realCoreExecutorRestoresPrivateRouteOnSuccessAndMappingFailure(String mapping) {
        config.setMode("saas");
        config.getTenant().setEnable(true);
        config.getTenant().setMode("schema");
        user.setTenantId(23L);
        TenantContext.bind(23L, "tenant_expected");
        java.util.concurrent.atomic.AtomicReference<String> path =
                new java.util.concurrent.atomic.AtomicReference<>("tenant_expected,eva_core");
        when(jdbc.queryForObject("SELECT current_setting('search_path')", String.class))
                .thenAnswer(invocation -> path.get());
        when(jdbc.queryForObject(eq("SELECT pg_catalog.set_config('search_path', ?, true)"),
                eq(String.class), anyString())).thenAnswer(invocation -> {
                    path.set(invocation.getArgument(2));
                    return path.get();
                });
        when(jdbc.queryForMap(anyString(), anyString())).thenAnswer(invocation -> Map.of(
                "current_schema_name", path.get().split(",")[0],
                "relation_schema_name", path.get().split(",")[0]));
        when(resolver.resolve(23L)).thenReturn("missing".equals(mapping) ? null : mapping);
        DictItemSnapshot actual = new DictItemSnapshot(mapper, jdbc, config,
                new CoreSchemaExecutor(jdbc, config), resolver);
        ThreadUserHelper.runWithUser(user, () -> {
            if ("tenant_expected".equals(mapping)) {
                assertEquals(11L, actual.load(1L).items().getFirst().getId());
            } else {
                assertThrows(RuntimeException.class, () -> actual.load(1L));
                verify(mapper, never()).selectOne(any(Wrapper.class));
                verify(jdbc, never()).query(anyString(), any(RowMapper.class), any(Long.class));
            }
            assertEquals("tenant_expected,eva_core", path.get());
            assertEquals(23L, TenantContext.tenantId());
            assertEquals("tenant_expected", TenantContext.schemaName());
        });
    }

    private DictAoeBo bo() {
        DictAoeBo input = new DictAoeBo();
        input.setId(1L);
        input.setCode("status");
        input.setName("状态");
        return input;
    }
}
