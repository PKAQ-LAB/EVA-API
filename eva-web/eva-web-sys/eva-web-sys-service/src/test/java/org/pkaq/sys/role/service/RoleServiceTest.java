package org.pkaq.sys.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pkaq.sys.module.convert.ModuleConvert;
import org.pkaq.sys.organization.mapper.OrganizationMapper;
import org.pkaq.sys.organization.entity.OrganizationEntity;
import org.pkaq.sys.role.bo.RoleAoeBo;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.TenantProperties;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.sys.module.mapper.ModuleMapper;
import org.pkaq.sys.role.bo.RoleUserRefBo;
import org.pkaq.sys.role.bo.RoleResourceRefBo;
import org.pkaq.sys.role.convert.RoleConvert;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.role.mapper.RoleMapper;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.role.vo.RoleGrantedUserVo;
import org.pkaq.sys.tenant.mapper.TenantResourceMapper;
import org.pkaq.sys.user.convert.UserConvert;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.doAnswer;

/**
 * 角色用户授权的数据权限测试。
 *
 * @author PKAQ
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
    @Mock
    private OrganizationMapper organizationMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private RoleResourceMapper roleResourceMapper;
    @Mock
    private RoleUserMapper roleUserMapper;
    @Mock
    private ModuleMapper moduleMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private TenantResourceMapper tenantResourceMapper;
    @Mock
    private UserConvert userConvert;
    @Mock
    private RoleConvert roleConvert;
    @Mock
    private ModuleConvert moduleConvert;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private CoreSchemaExecutor coreSchemaExecutor;
    @Mock
    private EvaConfig evaConfig;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private RoleService service;

    /**
     * 初始化被测服务。
     */
    @BeforeEach
    void setUp() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), RoleUserEntity.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), UserEntity.class);
        service = new RoleService(organizationMapper, roleResourceMapper, roleUserMapper, moduleMapper, userMapper,
                tenantResourceMapper, userConvert, roleConvert, moduleConvert, eventPublisher,
                coreSchemaExecutor, evaConfig, jdbcTemplate);
        ReflectionTestUtils.setField(service, "mapper", roleMapper);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), RoleEntity.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), OrganizationEntity.class);
        org.mockito.Mockito.lenient().when(evaConfig.isSaasMode()).thenReturn(true);
    }

    @Test
    void shouldRejectMissingBlankUnknownAndUnsupportedNewScopesWithoutWriting() {
        for (String scope : java.util.Arrays.asList(null, "", " ", "unknown", "0004", "0005", "0006")) {
            RoleAoeBo bo = roleBo(scope);
            assertThrows(RuntimeException.class, () -> service.editUniqueCode(bo));
        }
        verifyNoInteractions(roleMapper, roleConvert, roleResourceMapper, roleUserMapper, userMapper);
    }

    @Test
    void shouldPreserveOmittedScopeAndOrganizationsIncludingPreviouslyInvalidValues() {
        for (String scope : java.util.Arrays.asList("0000", "0001", "0003", null, "invalid", "0004")) {
            reset(roleMapper, roleConvert, roleUserMapper);
            RoleEntity origin = new RoleEntity();
            origin.setDataScope(scope);
            origin.setDataOrgIds("12,13");
            when(roleMapper.selectById(5L)).thenReturn(origin);
            RoleAoeBo bo = roleBo(null);
            bo.setId(5L);
            bo.setDataOrgIds("999");
            doAnswer(invocation -> {
                RoleAoeBo actual = invocation.getArgument(0);
                assertEquals(scope, actual.getDataScope());
                assertEquals("12,13", actual.getDataOrgIds());
                return origin;
            }).when(roleConvert).fromBo(any(RoleAoeBo.class));

            service.editUniqueCode(bo);

            verify(roleMapper).insertOrUpdate(origin);
        }
        verifyNoInteractions(organizationMapper);
    }

    @Test
    void shouldRejectExplicitBlankInvalidAndUnsupportedEditsBeforeWriting() {
        RoleEntity origin = new RoleEntity();
        origin.setDataScope("0000");
        when(roleMapper.selectById(5L)).thenReturn(origin);
        for (String scope : List.of("", " ", "invalid", "0004", "0005", "0006")) {
            RoleAoeBo bo = roleBo(scope);
            bo.setId(5L);
            assertThrows(RuntimeException.class, () -> service.editUniqueCode(bo));
        }
        verify(roleMapper, never()).insertOrUpdate(any(RoleEntity.class));
        verifyNoInteractions(roleConvert, roleUserMapper, userMapper);
    }

    @Test
    void shouldRejectInvalidAndPartlyInvisibleCustomOrganizationsWithoutWriting() {
        for (String ids : java.util.Arrays.asList(null, "", " ", "0", "-1", "1,no", "1,", ",1",
                "99999999999999999999999")) {
            RoleAoeBo bo = roleBo("0003");
            bo.setDataOrgIds(ids);
            assertThrows(RuntimeException.class, () -> service.editUniqueCode(bo));
        }
        RoleAoeBo bo = roleBo("0003");
        bo.setDataOrgIds("12,13");
        OrganizationEntity visible = new OrganizationEntity();
        visible.setId(12L);
        when(organizationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(visible));
        assertThrows(RuntimeException.class, () -> service.editUniqueCode(bo));
        verifyNoInteractions(roleMapper, roleConvert, roleUserMapper, userMapper);
    }

    @Test
    void shouldKeepCustomOrganizationsWhenScopeUnchangedAndRefreshAffectedUsers() {
        RoleEntity origin = new RoleEntity();
        origin.setDataScope("0003");
        origin.setDataOrgIds("12");
        when(roleMapper.selectById(5L)).thenReturn(origin);
        OrganizationEntity visible = new OrganizationEntity();
        visible.setId(12L);
        when(organizationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(visible));
        when(roleConvert.fromBo(any(RoleAoeBo.class))).thenReturn(origin);
        when(roleUserMapper.selectObjs(any(LambdaQueryWrapper.class))).thenReturn(List.of(10L));
        RoleAoeBo bo = roleBo("0003");
        bo.setId(5L);

        ThreadUserHelper.runWithUser(new ThreadUser().setUserId(11L).setTenantId(22L),
                () -> service.editUniqueCode(bo));

        assertEquals("12", bo.getDataOrgIds());
        verify(roleMapper).insertOrUpdate(origin);
        verify(userMapper).incrementPermVer(10L);
    }

    private RoleAoeBo roleBo(String scope) {
        RoleAoeBo bo = new RoleAoeBo();
        bo.setCode("TEST");
        bo.setName("测试角色");
        bo.setDataScope(scope);
        return bo;
    }

    @Test
    void realConvertRemarkEditsPreserveTrustedFrozenScopeOrganizationsAndPermissionRefresh() {
        ReflectionTestUtils.setField(service, "roleConvert", org.mapstruct.factory.Mappers.getMapper(RoleConvert.class));
        for (org.pkaq.core.enums.FrozenEnumm state : List.of(org.pkaq.core.enums.FrozenEnumm.UN_FROZEN,
                org.pkaq.core.enums.FrozenEnumm.FROZEN)) {
            for (Integer submitted : java.util.Arrays.asList(null, 0, 1, 9999)) {
                reset(roleMapper, roleUserMapper, userMapper);
                RoleEntity origin = new RoleEntity();
                origin.setId(5L);
                origin.setFrozen(state);
                origin.setDataScope("0000");
                origin.setDataOrgIds("12,13");
                when(roleMapper.selectById(5L)).thenReturn(origin);
                when(roleUserMapper.selectObjs(any(LambdaQueryWrapper.class))).thenReturn(List.of(10L));
                RoleAoeBo bo = roleBo(null);
                bo.setId(5L);
                bo.setFrozen(submitted);
                bo.setRemark("合成备注修改");

                service.editUniqueCode(bo);

                org.mockito.ArgumentCaptor<RoleEntity> captor = org.mockito.ArgumentCaptor.forClass(RoleEntity.class);
                verify(roleMapper).insertOrUpdate(captor.capture());
                RoleEntity saved = captor.getValue();
                assertEquals(state, saved.getFrozen());
                assertEquals("合成备注修改", saved.getRemark());
                assertEquals("测试角色", saved.getName());
                assertEquals("0000", saved.getDataScope());
                assertEquals("12,13", saved.getDataOrgIds());
                verify(userMapper).incrementPermVer(10L);
            }
        }
        verifyNoInteractions(roleResourceMapper, organizationMapper);
    }

    @Test
    void realConvertDoesNotBypassReadOnlyRoleOrInvalidFrozenInput() {
        ReflectionTestUtils.setField(service, "roleConvert", org.mapstruct.factory.Mappers.getMapper(RoleConvert.class));
        RoleEntity origin = new RoleEntity();
        origin.setFrozen(org.pkaq.core.enums.FrozenEnumm.READ_ONLY);
        when(roleMapper.selectById(5L)).thenReturn(origin);
        RoleAoeBo bo = roleBo("0000");
        bo.setId(5L);
        bo.setFrozen(0);
        assertThrows(org.pkaq.core.exception.BizException.class, () -> service.editUniqueCode(bo));
        origin.setFrozen(org.pkaq.core.enums.FrozenEnumm.UN_FROZEN);
        bo.setFrozen(2);
        assertThrows(org.pkaq.core.exception.BizException.class, () -> service.editUniqueCode(bo));
        verify(roleMapper, never()).insertOrUpdate(any(RoleEntity.class));
        verifyNoInteractions(roleUserMapper, userMapper, roleResourceMapper, organizationMapper);
    }

    @Test
    void dedicatedFrozenSwitchStillTogglesAndSkipsReadOnlyRoles() {
        for (org.pkaq.core.enums.FrozenEnumm state : org.pkaq.core.enums.FrozenEnumm.values()) {
            reset(roleMapper, roleUserMapper);
            RoleEntity origin = new RoleEntity();
            origin.setFrozen(state);
            when(roleMapper.selectById(5L)).thenReturn(origin);
            org.pkaq.core.mvc.bo.SingleArray<Long> ids = new org.pkaq.core.mvc.bo.SingleArray<>();
            ids.setParam(Set.of(5L));

            service.switchFrozen(ids);

            if (state == org.pkaq.core.enums.FrozenEnumm.READ_ONLY) {
                verify(roleMapper, never()).update(any(), any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
            } else {
                org.mockito.ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<RoleEntity>>
                        captor = org.mockito.ArgumentCaptor.forClass(
                                com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper.class);
                verify(roleMapper).update(org.mockito.ArgumentMatchers.isNull(), captor.capture());
                org.junit.jupiter.api.Assertions.assertTrue(captor.getValue().getSqlSet().contains("frozen"));
                org.pkaq.core.enums.FrozenEnumm target = state == org.pkaq.core.enums.FrozenEnumm.FROZEN
                        ? org.pkaq.core.enums.FrozenEnumm.UN_FROZEN : org.pkaq.core.enums.FrozenEnumm.FROZEN;
                org.junit.jupiter.api.Assertions.assertTrue(captor.getValue().getParamNameValuePairs().containsValue(target));
            }
        }
    }

    @Test
    void shouldRouteOrganizationValidationWithinTransactionAndRollbackRejectedSave() throws Exception {
        javax.sql.DataSource dataSource = org.mockito.Mockito.mock(javax.sql.DataSource.class);
        java.sql.Connection connection = org.mockito.Mockito.mock(java.sql.Connection.class);
        java.sql.PreparedStatement statement = org.mockito.Mockito.mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet resultSet = org.mockito.Mockito.mock(java.sql.ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(connection.prepareStatement(any(String.class))).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getString(1)).thenReturn("original");
        EvaConfig config = new EvaConfig();
        config.getTenant().setEnable(true);
        config.getTenant().setMode(TenantProperties.MODE_SCHEMA);
        org.pkaq.core.mybatis.tenant.TenantSchemaResolver resolver =
                org.mockito.Mockito.mock(org.pkaq.core.mybatis.tenant.TenantSchemaResolver.class);
        when(resolver.resolve(22L)).thenReturn("tenant_22");
        org.pkaq.core.mybatis.tenant.TenantSchemaRouter router =
                new org.pkaq.core.mybatis.tenant.TenantSchemaRouter(dataSource, config, resolver);
        org.springframework.aop.aspectj.annotation.AspectJProxyFactory factory =
                new org.springframework.aop.aspectj.annotation.AspectJProxyFactory(service);
        factory.setProxyTargetClass(true);
        org.springframework.transaction.interceptor.TransactionInterceptor transactionInterceptor =
                new org.springframework.transaction.interceptor.TransactionInterceptor();
        transactionInterceptor.setTransactionManager(
                new org.springframework.jdbc.datasource.DataSourceTransactionManager(dataSource));
        transactionInterceptor.setTransactionAttributeSource(
                new org.springframework.transaction.annotation.AnnotationTransactionAttributeSource());
        factory.addAdvice(transactionInterceptor);
        factory.addAspect(new org.pkaq.core.mybatis.tenant.TenantSchemaTransactionalAspect(router));
        RoleService proxy = factory.getProxy();
        when(organizationMapper.selectList(any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            LambdaQueryWrapper<OrganizationEntity> query = invocation.getArgument(0);
            org.junit.jupiter.api.Assertions.assertTrue(query.getSqlSegment().contains("tenant_id"));
            org.junit.jupiter.api.Assertions.assertTrue(query.getParamNameValuePairs().containsValue(22L));
            assertEquals("tenant_22", org.pkaq.core.tenant.TenantContext.schemaName());
            assertEquals(true, org.springframework.transaction.support.TransactionSynchronizationManager
                    .isActualTransactionActive());
            return List.of();
        });
        RoleAoeBo bo = roleBo("0003");
        bo.setDataOrgIds("13");

        ThreadUserHelper.runWithUser(new ThreadUser().setUserId(11L).setTenantId(22L),
                () -> assertThrows(RuntimeException.class, () -> proxy.editUniqueCode(bo)));

        verify(organizationMapper).selectList(any(LambdaQueryWrapper.class));
        verify(resolver).resolve(22L);
        verify(connection).rollback();
        verify(connection, never()).commit();
        verifyNoInteractions(roleMapper, roleConvert, userMapper);
        assertEquals(null, org.pkaq.core.tenant.TenantContext.schemaName());
    }

    @Test
    void shouldSaveExplicitAllWithoutInventingOrganizationDefaults() {
        RoleAoeBo bo = roleBo("0000");
        bo.setDataOrgIds("");
        RoleEntity entity = new RoleEntity();
        entity.setDataScope("0000");
        when(roleConvert.fromBo(bo)).thenReturn(entity);

        service.editUniqueCode(bo);

        verify(roleMapper).insertOrUpdate(entity);
        verifyNoInteractions(organizationMapper);
        assertEquals("0000", bo.getDataScope());
    }

    /**
     * 角色详情只返回当前数据权限范围内的已选用户。
     */
    @Test
    void shouldHideCheckedUsersOutsideDataScope() {
        when(roleMapper.selectById(5L)).thenReturn(new RoleEntity());
        UserEntity visibleUser = new UserEntity();
        visibleUser.setId(10L);
        when(userMapper.selectManagedList(any(), any())).thenReturn(List.of(visibleUser));
        when(roleUserMapper.selectObjs(any(LambdaQueryWrapper.class))).thenReturn(List.of(10L, 20L));
        when(userConvert.entityToSimpleVo(List.of(visibleUser))).thenReturn(List.of());
        RoleGrantedUserVo expected = new RoleGrantedUserVo();
        when(roleConvert.toGrantedUserVo(List.of(), Set.of(10L))).thenReturn(expected);

        RoleGrantedUserVo result = service.listUser(5L, null);

        assertEquals(expected, result);
        verify(roleConvert).toGrantedUserVo(List.of(), Set.of(10L));
    }

    /**
     * 保存授权时不得撤销当前数据权限范围外的既有用户。
     */
    @Test
    void shouldPreserveExistingUsersOutsideDataScope() {
        when(roleMapper.selectById(5L)).thenReturn(new RoleEntity());
        when(roleUserMapper.selectObjs(any(LambdaQueryWrapper.class))).thenReturn(List.of(10L, 20L));
        UserEntity visibleUser = new UserEntity();
        visibleUser.setId(10L);
        when(userMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(visibleUser), List.of(visibleUser));
        RoleUserRefBo input = new RoleUserRefBo();
        input.setRoleId(5L);
        input.setUserId(List.of(10L));

        service.grantUser(input);

        verify(roleUserMapper, never()).delete(any(LambdaQueryWrapper.class));
        verify(roleUserMapper, never()).insert(any(RoleUserEntity.class));
        verify(userMapper, never()).incrementPermVer(any());
    }

    /**
     * schema 模式只允许保存平台授予当前租户且仍有效的资源。
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldValidateSchemaRoleResourcesInCoreSchema() {
        TenantProperties tenant = new TenantProperties();
        tenant.setEnable(true);
        tenant.setMode(TenantProperties.MODE_SCHEMA);
        when(evaConfig.getTenant()).thenReturn(tenant);
        when(roleMapper.selectById(5L)).thenReturn(new RoleEntity());
        when(roleResourceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(tenantResourceMapper.selectAuthorizedResourceIds(101L)).thenReturn(Set.of(7L));
        when(roleResourceMapper.selectValidResourceIds(Set.of(7L))).thenReturn(Set.of(7L));
        when(coreSchemaExecutor.execute(any())).thenAnswer(invocation -> {
            Function<JdbcTemplate, Object> callback = invocation.getArgument(0);
            return callback.apply(null);
        });
        RoleResourceRefBo input = new RoleResourceRefBo();
        input.setRoleId(5L);
        input.setResourceId(List.of(7L));

        ThreadUserHelper.runWithUser(new ThreadUser().setTenantId(101L), () -> service.grantResource(input));

        verify(coreSchemaExecutor).execute(any());
        verify(roleResourceMapper).insert(any(org.pkaq.sys.role.entity.RoleResourceEntity.class));
    }

    /**
     * schema 模式不得突破平台授予当前租户的资源上限。
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldRejectResourceOutsideTenantEffectiveGrant() {
        TenantProperties tenant = new TenantProperties();
        tenant.setEnable(true);
        tenant.setMode(TenantProperties.MODE_SCHEMA);
        when(evaConfig.getTenant()).thenReturn(tenant);
        when(roleMapper.selectById(5L)).thenReturn(new RoleEntity());
        when(roleResourceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(tenantResourceMapper.selectAuthorizedResourceIds(101L)).thenReturn(Set.of(7L));
        when(coreSchemaExecutor.execute(any())).thenAnswer(invocation -> {
            Function<JdbcTemplate, Object> callback = invocation.getArgument(0);
            return callback.apply(null);
        });
        RoleResourceRefBo input = new RoleResourceRefBo();
        input.setRoleId(5L);
        input.setResourceId(List.of(8L));

        assertThrows(RuntimeException.class, () -> ThreadUserHelper.runWithUser(
                new ThreadUser().setTenantId(101L), () -> service.grantResource(input)));

        verify(roleResourceMapper, never()).insert(any(org.pkaq.sys.role.entity.RoleResourceEntity.class));
    }
}
