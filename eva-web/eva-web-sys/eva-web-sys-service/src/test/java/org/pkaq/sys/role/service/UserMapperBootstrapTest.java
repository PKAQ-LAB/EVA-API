package org.pkaq.sys.role.service;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMapping;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.parallel.Isolated;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mybatis.enums.UniversalEnumTypeHandler;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.json.JacksonObjectMapper;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.vo.UserDetailVo;
import org.pkaq.config.MybatisPlusConfig;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * C映射的真实MyBatis自动配置、加载顺序和合成JDBC回归。
 * @author PKAQ
 * @date 2026-10-27
 */
@Isolated
class UserMapperBootstrapTest {
    @AfterEach
    void cleanContext() {
        TenantContext.clear();
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertTrue(TransactionSynchronizationManager.getResourceMap().isEmpty());
        assertNull(ThreadUserHelper.getCurrentUserOrNull());
    }

    @Test
    void onlyRegisteredConfigurationEnhancementAndItsTwoFactoryMethodsAreAllowed() {
        // 只注册内存配置和属性对象，取得Spring真实增强形态，不加载数据源或应用扫描。
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(EvaConfig.class, EvaConfig::new);
            context.register(MybatisPlusConfig.class);
            context.refresh();
            BeanDefinition original = context.getBeanFactory().getBeanDefinition("mybatisPlusConfig");
            Class<?> actual = ((AbstractBeanDefinition) original).getBeanClass();
            DefaultListableBeanFactory accepted = configurationRegistry("mybatisPlusConfig", original);
            for (String method : List.of("mybatisPlusInterceptor", "getDatabaseIdProvider")) {
                RootBeanDefinition factory = new RootBeanDefinition();
                factory.setFactoryBeanName("mybatisPlusConfig");
                factory.setFactoryMethodName(method);
                accepted.registerBeanDefinition(method, factory);
            }
            assertDoesNotThrow(() -> UserMapperBootstrapFixture.assertBeanDefinitions(accepted));
            assertThrows(AssertionError.class, () -> UserMapperBootstrapFixture.assertBeanDefinitions(
                    configurationRegistry("wrongConfig", original)));

            AnnotatedGenericBeanDefinition wrongMetadata = new AnnotatedGenericBeanDefinition(EvaConfig.class);
            wrongMetadata.setBeanClass(actual);
            assertRejectedConfiguration(wrongMetadata);
            AnnotatedGenericBeanDefinition wrongParent = new AnnotatedGenericBeanDefinition(MybatisPlusConfig.class);
            wrongParent.setBeanClass(WrongParent$$Enhancement.class);
            assertRejectedConfiguration(wrongParent);
            AnnotatedGenericBeanDefinition unaware = new AnnotatedGenericBeanDefinition(MybatisPlusConfig.class);
            unaware.setBeanClass(UnawareConfig$$Enhancement.class);
            assertRejectedConfiguration(unaware);
            AnnotatedGenericBeanDefinition factoryConfig = new AnnotatedGenericBeanDefinition(MybatisPlusConfig.class);
            factoryConfig.setBeanClass(actual);
            factoryConfig.setFactoryBeanName("forbiddenOwner");
            factoryConfig.setFactoryMethodName("forbiddenFactory");
            assertRejectedConfiguration(factoryConfig);
            for (String method : List.of("sqlSessionFactory", "forbiddenFactory")) {
                DefaultListableBeanFactory rejected = configurationRegistry("mybatisPlusConfig", original);
                RootBeanDefinition factory = new RootBeanDefinition();
                factory.setFactoryBeanName("mybatisPlusConfig");
                factory.setFactoryMethodName(method);
                rejected.registerBeanDefinition("unapprovedFactory", factory);
                assertThrows(AssertionError.class, () -> UserMapperBootstrapFixture.assertBeanDefinitions(rejected));
            }
        }
    }

    private DefaultListableBeanFactory configurationRegistry(String name, BeanDefinition definition) {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        registry.registerBeanDefinition(name, definition);
        return registry;
    }

    private void assertRejectedConfiguration(BeanDefinition definition) {
        assertThrows(AssertionError.class, () -> UserMapperBootstrapFixture.assertBeanDefinitions(
                configurationRegistry("mybatisPlusConfig", definition)));
    }

    /**
     * 仅用于负向类身份校验，不创建实例。
     * @author PKAQ
     * @date 2026-10-27
     */
    private static final class WrongParent$$Enhancement extends EvaConfig {
    }

    /**
     * 模拟含增强分隔符但缺少公开增强关系的子类，不创建实例。
     * @author PKAQ
     * @date 2026-10-27
     */
    private static final class UnawareConfig$$Enhancement extends MybatisPlusConfig {
        private UnawareConfig$$Enhancement() {
            super(new EvaConfig());
        }
    }

    @Test
    void legacyUserBeforeCommonKeepsGeneratedStatementAndLosesProfile() throws Exception {
        try (var fixture = fixture(UserMapperBootstrapFixture.Order.LEGACY_USER_FIRST)) {
            MappedStatement statement = statement(fixture);
            assertTrue(statement.getResource().contains("UserMapper.java"));
            assertFalse(boundSql(statement, fixture.target).contains("JOIN"));
            UserEntity entity = readAccount(fixture);
            assertEquals(fixture.target, entity.getId());
            assertNull(entity.getDeptId());
            assertNull(entity.getCode());
            assertEquals(0, fixture.writes);
        }
    }

    @ParameterizedTest
    @EnumSource(value = UserMapperBootstrapFixture.Order.class,
            names = {"NORMAL", "USER_FIRST", "COMMON_FIRST"})
    void independentBootstrapsKeepXmlJoinAndAllMappings(UserMapperBootstrapFixture.Order order) throws Exception {
        try (var fixture = fixture(order)) {
            MappedStatement statement = statement(fixture);
            assertTrue(statement.getResource().replace('\\', '/').contains("mapper/User.xml"));
            String query = boundSql(statement, fixture.target);
            assertTrue(query.contains("LEFT JOIN SYS_ACCOUNT_PROFILE"));
            assertTrue(query.contains("P.DEPT_ID"));
            assertTrue(query.contains("COALESCE(U.DELETED, 0) = 0"));
            assertTrue(fixture.bootstrap.resources.stream().noneMatch(item -> item.contains("fixtures/")));
            var configuration = fixture.factory.getConfiguration();
            var mappings = configuration.getResultMap("org.pkaq.sys.user.mapper.UserMapper.BaseResultMap")
                    .getResultMappings();
            Set<String> expected = Set.of("id", "revision", "deleted", "frozen", "sort", "tenantId",
                    "createId", "createBy", "utcCreate", "modifyId", "modifyBy", "utcModify", "remark",
                    "code", "name", "deptId");
            assertEquals(16, mappings.size());
            assertEquals(expected, new HashSet<>(mappings.stream().map(ResultMapping::getProperty).toList()));
            var inherited = configuration.getResultMap("common.resultMap").getResultMappings();
            assertEquals(13, inherited.size());
            for (ResultMapping parent : inherited) {
                ResultMapping child = mappings.stream().filter(item -> item.getProperty().equals(parent.getProperty()))
                        .findFirst().orElseThrow();
                assertEquals(parent.getColumn().toLowerCase(), child.getColumn().toLowerCase());
                assertEquals(parent.getJavaType(), child.getJavaType());
                assertEquals(parent.getTypeHandler().getClass(), child.getTypeHandler().getClass());
            }
            assertTrue(configuration.getTypeHandlerRegistry().getTypeHandler(FrozenEnumm.class)
                    instanceof UniversalEnumTypeHandler<?>);
            assertEquals(1, configuration.getInterceptors().size());
            assertTrue(configuration.getIncompleteResultMaps().isEmpty());
            assertTrue(configuration.getIncompleteStatements().isEmpty());
            assertTrue(fixture.metadataReads > 0);
            assertTrue(fixture.connectionRequests > 0);
            assertMappedAccount(readAccount(fixture), fixture.target);
            assertEquals(0, fixture.writes);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 9999})
    void frozenCodesUseExistingHandler(int state) throws Exception {
        try (var fixture = fixture(UserMapperBootstrapFixture.Order.USER_FIRST)) {
            fixture.accountFrozen = state;
            assertEquals(state, readAccount(fixture).getFrozen().getCode());
        }
    }

    @Test
    void nullAndUnknownFrozenKeepHandlerSemantics() throws Exception {
        try (var fixture = fixture(UserMapperBootstrapFixture.Order.USER_FIRST)) {
            fixture.accountValues.put("frozen", null);
            assertNull(readAccount(fixture).getFrozen());
            fixture.accountValues.put("frozen", 7654);
            assertThrows(RuntimeException.class, () -> readAccount(fixture));
            assertEquals(0, fixture.writes);
        }
    }

    @Test
    void optionalProfileAndNullDeletedRemainReadable() throws Exception {
        try (var fixture = fixture(UserMapperBootstrapFixture.Order.NORMAL)) {
            fixture.profilePresent = false;
            fixture.accountDeleted = null;
            UserEntity entity = readAccount(fixture);
            assertNotNull(entity);
            assertEquals("synthetic-worker", entity.getAccount());
            assertNull(entity.getDeptId());
            assertNull(entity.getCode());
            assertNull(entity.getName());
            assertEquals(0, fixture.writes);
            verifyNoInteractions(fixture.profiles, fixture.accountCreation);
        }
    }

    @Test
    void missingTenantColumnDoesNotInventZeroAndOtherStatementsKeepMappings() throws Exception {
        try (var fixture = fixture(UserMapperBootstrapFixture.Order.NORMAL)) {
            fixture.tenantColumnPresent = false;
            assertNull(readAccount(fixture).getTenantId());
            var configuration = fixture.factory.getConfiguration();
            String namespace = "org.pkaq.sys.user.mapper.UserMapper.";
            for (String method : List.of("selectById", "selectManagedList", "selectManagedPage")) {
                MappedStatement statement = configuration.getMappedStatement(namespace + method);
                assertTrue(statement.getResource().replace('\\', '/').contains("mapper/User.xml"));
                assertEquals(namespace + "BaseResultMap", statement.getResultMaps().getFirst().getId());
                assertTrue(statement.getBoundSql(Map.of("id", fixture.target)).getSql()
                        .contains("LEFT JOIN SYS_ACCOUNT_PROFILE"));
            }
            MappedStatement nested = configuration.getMappedStatement(namespace + "getUserWithModuleAndRoleById");
            assertEquals(namespace + "UserWithModuleAndRole", nested.getResultMaps().getFirst().getId());
            assertTrue(nested.getResultMaps().getFirst().getResultMappings().stream()
                    .anyMatch(item -> "roles".equals(item.getProperty()) && item.getNestedQueryId() != null));
            assertTrue(nested.getResultMaps().getFirst().getResultMappings().stream()
                    .anyMatch(item -> "modules".equals(item.getProperty()) && item.getNestedQueryId() != null));
        }
    }

    @Test
    void syntheticJdbcRejectsUnknownCallsWritesBatchesAndOverloadedSetters() throws Exception {
        try (var fixture = fixture(UserMapperBootstrapFixture.Order.NORMAL)) {
            assertThrows(AssertionError.class, () -> fixture.dataSource.getConnection("forbidden", "forbidden"));
            assertThrows(AssertionError.class, () -> fixture.connection.prepareCall("CALL forbidden()"));
            assertEquals("PostgreSQL", fixture.connection.getMetaData().getDatabaseProductName());
            assertFalse(fixture.connection.getMetaData().supportsMultipleResultSets());
            assertThrows(AssertionError.class, () -> fixture.connection.getMetaData().getURL());
            TransactionTemplate transaction = new TransactionTemplate(
                    new DataSourceTransactionManager(fixture.dataSource));
            transaction.setReadOnly(true);
            transaction.executeWithoutResult(status -> {
                try {
                    String query = boundSql(statement(fixture), fixture.target);
                    try (PreparedStatement prepared = fixture.connection.prepareStatement(query);
                            Statement plain = fixture.connection.createStatement()) {
                        assertThrows(AssertionError.class, prepared::getWarnings);
                        assertThrows(AssertionError.class,
                                () -> prepared.setObject(1, "forbidden", java.sql.Types.VARCHAR));
                        assertThrows(AssertionError.class,
                                () -> prepared.setAsciiStream(1, java.io.InputStream.nullInputStream()));
                        assertThrows(AssertionError.class, prepared::executeUpdate);
                        assertThrows(AssertionError.class, prepared::executeLargeUpdate);
                        assertThrows(AssertionError.class, prepared::addBatch);
                        assertThrows(AssertionError.class, prepared::executeBatch);
                        assertThrows(AssertionError.class, prepared::executeLargeBatch);
                        assertThrows(AssertionError.class, () -> plain.execute("UPDATE forbidden SET id=1"));
                        assertThrows(AssertionError.class, () -> plain.executeUpdate("UPDATE forbidden SET id=1"));
                        assertThrows(AssertionError.class, () -> plain.executeLargeUpdate("UPDATE forbidden SET id=1"));
                        assertThrows(AssertionError.class, () -> plain.addBatch("DELETE FROM forbidden"));
                        assertThrows(AssertionError.class, plain::executeBatch);
                        assertThrows(AssertionError.class, plain::executeLargeBatch);
                        assertThrows(AssertionError.class, plain::getWarnings);
                        prepared.setLong(1, fixture.target);
                        try (ResultSet result = prepared.executeQuery()) {
                            assertEquals(ResultSet.TYPE_FORWARD_ONLY, result.getType());
                            assertTrue(result.next());
                            assertThrows(AssertionError.class, () -> result.getObject("id", Map.of()));
                            assertThrows(AssertionError.class, () -> result.getMetaData().isAutoIncrement(1));
                            assertThrows(AssertionError.class, result::getWarnings);
                            assertThrows(AssertionError.class, () -> result.updateString("account", "forbidden"));
                            assertThrows(AssertionError.class, result::updateRow);
                            assertThrows(AssertionError.class, result::insertRow);
                            assertThrows(AssertionError.class, result::deleteRow);
                        }
                    }
                } catch (java.sql.SQLException failure) {
                    throw new AssertionError("合成JDBC负向验证失败", failure);
                }
            });
            assertTrue(fixture.writes >= 11);
        }
        try (var manual = new AssignedRoleDetailJdbcFixture(true)) {
            assertNull(manual.connection.getMetaData());
        }
    }

    @Test
    void deletedOrHiddenAccountDoesNotReadRelations() throws Exception {
        try (var fixture = fixture(UserMapperBootstrapFixture.Order.NORMAL)) {
            fixture.accountDeleted = 123L;
            assertThrows(RuntimeException.class, () -> readDetail(fixture, "0000"));
            fixture.accountDeleted = 0L;
            assertThrows(RuntimeException.class, () -> readDetail(fixture, ""));
            assertEquals(0, fixture.referenceReads);
            assertEquals(0, fixture.writes);
        }
    }

    @Test
    void detailsKeepRawRoleSummaryAndPostsInSameTransaction() throws Exception {
        try (var fixture = fixture(UserMapperBootstrapFixture.Order.NORMAL)) {
            fixture.assigned.addAll(List.of(12L, 13L));
            fixture.role("eva", 12L, "合成只读角色", "raw-legacy", 9999, 0L, 7L);
            fixture.role("eva", 13L, "已删除角色", "deleted-role", 0, 44L, 7L);
            fixture.posts.add(31L);
            UserDetailVo detail = readDetail(fixture, "0000");
            assertEquals(42L, detail.getDeptId());
            assertEquals(fixture.assigned, detail.getRoleIds());
            assertEquals(List.of(31L), detail.getPostId());
            assertEquals(1, detail.getAssignedRoles().size());
            assertEquals("raw-legacy", detail.getAssignedRoles().getFirst().getCode());
            assertEquals(9999, detail.getAssignedRoles().getFirst().getFrozen());
            String response = new JacksonObjectMapper().writeValueAsString(detail);
            assertFalse(response.contains("\"password\""));
            assertFalse(response.contains("synthetic-unused-hash"));
            assertEquals(1, fixture.referenceReads);
            assertEquals(0, fixture.controlPlaneLookups);
            assertTrue(fixture.schemaTransitions.isEmpty());
            assertTrue(fixture.queryOrigins.contains("sys_postuser_ref@eva"));
            fixture.namespaces.put("sys_postuser_ref", "third");
            int previousQueries = fixture.accountQueries;
            assertThrows(RuntimeException.class, () -> readDetail(fixture, "0000"));
            assertEquals(previousQueries, fixture.accountQueries);
            assertEquals(0, fixture.writes);
        }
    }

    static UserEntity readAccount(AssignedRoleDetailJdbcFixture fixture) {
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(fixture.dataSource));
        transaction.setReadOnly(true);
        AtomicReference<UserEntity> result = new AtomicReference<>();
        ThreadUserHelper.runWithUser(user("0000"),
                () -> result.set(transaction.execute(status -> fixture.users.selectById(fixture.target))));
        return result.get();
    }

    private AssignedRoleDetailJdbcFixture fixture(UserMapperBootstrapFixture.Order order) throws Exception {
        return new AssignedRoleDetailJdbcFixture(true, false, order);
    }

    private MappedStatement statement(AssignedRoleDetailJdbcFixture fixture) {
        return fixture.factory.getConfiguration().getMappedStatement("org.pkaq.sys.user.mapper.UserMapper.selectById");
    }

    private String boundSql(MappedStatement statement, long identifier) {
        return statement.getBoundSql(Map.of("id", identifier)).getSql();
    }

    private UserDetailVo readDetail(AssignedRoleDetailJdbcFixture fixture, String scope) {
        AtomicReference<UserDetailVo> result = new AtomicReference<>();
        ThreadUserHelper.runWithUser(user(scope), () -> result.set(fixture.service.getUser(fixture.target)));
        return result.get();
    }

    private static ThreadUser user(String scope) {
        return new ThreadUser().setUserId(7L).setTenantId(0L)
                .setDataScopes(List.of(new ThreadUser.DataScope(scope, List.of())));
    }

    private void assertMappedAccount(UserEntity entity, long identifier) {
        assertEquals(identifier, entity.getId());
        assertEquals(3, entity.getRevision());
        assertEquals(0L, entity.getDeleted());
        assertEquals(FrozenEnumm.UN_FROZEN, entity.getFrozen());
        assertEquals(1.5D, entity.getSort());
        assertEquals(0L, entity.getTenantId());
        assertEquals(7L, entity.getCreateId());
        assertEquals("合成创建人", entity.getCreateBy());
        assertEquals(LocalDateTime.of(2026, 10, 1, 8, 30), entity.getUtcCreate());
        assertEquals(7L, entity.getModifyId());
        assertEquals("合成修改人", entity.getModifyBy());
        assertEquals(LocalDateTime.of(2026, 10, 2, 9, 45), entity.getUtcModify());
        assertEquals("合成备注", entity.getRemark());
        assertEquals("fixture-worker", entity.getCode());
        assertEquals("合成档案", entity.getName());
        assertEquals(42L, entity.getDeptId());
        assertEquals("synthetic-worker", entity.getAccount());
        assertTrue("synthetic-unused-hash".equals(entity.getPassword()), "合成密码字段必须保持映射，不输出其值");
        assertEquals("synthetic-avatar", entity.getAvatar());
        assertEquals("合成昵称", entity.getNickName());
        assertEquals("synthetic-tel", entity.getTel());
        assertEquals("worker@example.invalid", entity.getEmail());
        assertEquals("192.0.2.1", entity.getLastIp());
        assertEquals(java.sql.Date.valueOf("2026-10-02"), entity.getLastLogin());
        assertEquals(9L, entity.getPermVer());
    }
}
