package org.pkaq.sys.module;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;
import org.pkaq.config.MybatisPlusConfig;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.mybatis.tenant.TenantSchemaRouter;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.module.mapper.AuthorizedMenuMapper;
import org.pkaq.sys.module.mapper.ModuleMapper;
import org.pkaq.sys.module.service.AuthorizedMenuService;
import org.pkaq.sys.module.vo.ModuleMenuVo;
import org.pkaq.test.PostgresInstanceEvidence;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 仅使用独立合成PostgreSQL验证真实菜单SQL、插件和租户边界，不读取应用连接配置。
 * @author PKAQ
 * @date 2026-10-28
 */
class AuthorizedMenuPostgresTest {
    @Test
    void keepsBusinessScopeButReturnsOnlyAuthorizedMetadata() throws Exception {
        try (Fixture fixture = new Fixture("menu-scope")) {
            for (String scope : List.of("0007", "0001", "0003")) {
                fixture.withUser(0L, scope, () -> {
                    assertTrue(fixture.oldMapper.listGrantedModules(9L).isEmpty());
                    if ("0007".equals(scope)) {
                        assertTrue(fixture.capture.queries.getLast().toUpperCase().contains("M.CREATE_ID"));
                    }
                    Collection<ModuleMenuVo> menus = fixture.service.fetchCurrentMenus();
                    assertEquals(1, menus.size());
                    ModuleMenuVo menu = menus.iterator().next();
                    assertEquals("/sys/organization", menu.getRouteUrl());
                    assertEquals(List.of(11L), menu.getResources().stream().map(resource -> resource.getId()).toList());
                    assertTrue(fixture.capture.queries.getLast().contains("RESOURCE_ID"));
                    assertTrue(!fixture.capture.queries.getLast().toUpperCase().contains("CREATE_ID"));
                    assertEquals(AuthorizedMenuMapper.class.getName() + ".selectMenuRows",
                            fixture.capture.preparedStatementIds.getLast());
                });
            }
        }
    }

    @Test
    void appliesDeletionFrozenAndEmptyAuthorizationWithoutLeakingSiblings() throws Exception {
        try (Fixture fixture = new Fixture("menu-state")) {
            for (String invalidation : List.of(
                    "UPDATE sys_role SET deleted=1", "UPDATE sys_role SET frozen=1",
                    "UPDATE sys_module SET deleted=1", "UPDATE sys_module SET frozen=1",
                    "UPDATE sys_module_resources SET deleted=1 WHERE id=11")) {
                fixture.jdbc.update(invalidation);
                fixture.withUser(0L, "0007", () -> assertTrue(fixture.service.fetchCurrentMenus().isEmpty()));
                fixture.jdbc.update("UPDATE sys_role SET deleted=NULL,frozen=9999");
                fixture.jdbc.update("UPDATE sys_module SET deleted=NULL,frozen=9999");
                fixture.jdbc.update("UPDATE sys_module_resources SET deleted=NULL");
            }
            fixture.jdbc.update("INSERT INTO sys_roleres_ref VALUES(1,11)");
            fixture.withUser(0L, "0007", () -> assertEquals(1,
                    fixture.service.fetchCurrentMenus().iterator().next().getResources().size()));
            fixture.jdbc.update("DELETE FROM sys_roleuser_ref WHERE user_id=9");
            fixture.withUser(0L, "0007", () -> assertTrue(fixture.service.fetchCurrentMenus().isEmpty()));
        }
    }

    @Test
    void routesSameUserIdThroughTenantAuthorizationAndCoreLimit() throws Exception {
        try (Fixture fixture = new Fixture("menu-tenant")) {
            fixture.config.getTenant().setEnable(true);
            fixture.config.getTenant().setMode("schema");
            fixture.config.getTenant().setCoreSchema("public");
            for (long tenantId : List.of(101L, 102L)) {
                fixture.jdbc.execute("CREATE SCHEMA tenant_" + tenantId);
                fixture.jdbc.execute("CREATE TABLE tenant_" + tenantId + ".sys_role (LIKE public.sys_role)");
                fixture.jdbc.execute("CREATE TABLE tenant_" + tenantId
                        + ".sys_roleuser_ref(role_id BIGINT,user_id BIGINT)");
                fixture.jdbc.execute("CREATE TABLE tenant_" + tenantId
                        + ".sys_roleres_ref(role_id BIGINT,resource_id BIGINT)");
                fixture.jdbc.update("INSERT INTO tenant_" + tenantId + ".sys_role VALUES(1,0,NULL,0)");
                fixture.jdbc.update("INSERT INTO tenant_" + tenantId + ".sys_roleuser_ref VALUES(1,9)");
            }
            fixture.jdbc.update("INSERT INTO tenant_101.sys_roleres_ref VALUES(1,11),(1,12)");
            fixture.jdbc.update("INSERT INTO tenant_102.sys_roleres_ref VALUES(1,12)");
            fixture.jdbc.update("INSERT INTO sys_tenant_resource VALUES(101,11),(102,12)");
            for (long tenantId : List.of(101L, 102L)) {
                fixture.withUser(tenantId, "0007", () -> {
                    assertEquals(List.of(tenantId == 101L ? 11L : 12L), fixture.service.fetchCurrentMenus()
                            .iterator().next().getResources().stream().map(resource -> resource.getId()).toList());
                    assertEquals("public", fixture.jdbc.queryForObject("SELECT current_schema()", String.class));
                    assertEquals(null, TenantContext.tenantId());
                });
            }
            fixture.withUser(101L, "0007", () -> {
                String outerPath = fixture.router.routeCurrentTransaction(101L);
                try {
                    String nestedPath = fixture.jdbc.queryForObject("SELECT current_setting('search_path')",
                            String.class);
                    assertEquals(1, fixture.service.fetchCurrentMenus().size());
                    assertEquals(nestedPath, fixture.jdbc.queryForObject(
                            "SELECT current_setting('search_path')", String.class));
                    assertEquals(101L, TenantContext.tenantId());
                    assertEquals("tenant_101", TenantContext.schemaName());
                    fixture.capture.beforeMenuPrepare = () -> {
                        assertEquals("public", fixture.jdbc.queryForObject("SELECT current_schema()", String.class));
                        throw new IllegalStateException("合成菜单prepare异常");
                    };
                    try {
                        RuntimeException failure = assertThrows(RuntimeException.class,
                                () -> fixture.service.fetchCurrentMenus());
                        Throwable cause = failure;
                        while (null != cause.getCause()) {
                            cause = cause.getCause();
                        }
                        assertEquals("合成菜单prepare异常", cause.getMessage());
                    } finally {
                        fixture.capture.beforeMenuPrepare = null;
                    }
                    assertEquals(nestedPath, fixture.jdbc.queryForObject(
                            "SELECT current_setting('search_path')", String.class));
                    assertEquals(101L, TenantContext.tenantId());
                    assertEquals("tenant_101", TenantContext.schemaName());
                    assertEquals(AuthorizedMenuMapper.class.getName() + ".selectMenuRows",
                            fixture.capture.preparedStatementIds.getLast());
                } finally {
                    fixture.router.restoreCurrentTransaction(outerPath);
                    TenantContext.clear();
                }
            });
            fixture.jdbc.execute("DROP TABLE tenant_102.sys_roleuser_ref");
            fixture.withUser(102L, "0007", () -> {
                assertThrows(SecurityException.class, () -> fixture.service.fetchCurrentMenus());
                assertEquals("public", fixture.jdbc.queryForObject("SELECT current_schema()", String.class));
                assertEquals(null, TenantContext.tenantId());
            });
            fixture.withUser(0L, "0007", () ->
                    assertThrows(SecurityException.class, () -> fixture.service.fetchCurrentMenus()));
        }
    }

    @Test
    void refusesMissingIdentityTransactionAndConflictingTenant() throws Exception {
        try (Fixture fixture = new Fixture("menu-identity")) {
            assertThrows(SecurityException.class, () -> fixture.service.fetchCurrentMenus());
            ThreadUserHelper.runWithUser(new ThreadUser().setUserId(9L).setTenantId(0L), () ->
                    assertThrows(SecurityException.class, () -> fixture.service.fetchCurrentMenus()));
            fixture.transaction.executeWithoutResult(status ->
                    assertThrows(SecurityException.class, () -> fixture.service.fetchCurrentMenus()));
            fixture.withUser(0L, "0007", () -> {
                TenantContext.bind(102L, "tenant_102");
                try {
                    assertThrows(SecurityException.class, () -> fixture.service.fetchCurrentMenus());
                } finally {
                    TenantContext.clear();
                }
            });
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final EmbeddedPostgres postgres;
        private final EvaConfig config = new EvaConfig();
        private final JdbcTemplate jdbc;
        private final TransactionTemplate transaction;
        private final AuthorizedMenuService service;
        private final ModuleMapper oldMapper;
        private final SqlCapture capture = new SqlCapture();
        private final TenantSchemaRouter router;

        private Fixture(String label) throws Exception {
            postgres = EmbeddedPostgres.builder().setServerConfig("listen_addresses", "127.0.0.1").start();
            try {
                DataSource dataSource = postgres.getPostgresDatabase();
                PostgresInstanceEvidence.capture(dataSource, label);
                jdbc = new JdbcTemplate(dataSource);
                config.getTenant().setStandaloneSchema("public");
                config.getDataPermission().setEnable(true);
                config.getDataPermission().setExcludeTables(List.of("sys_module_resources"));
                jdbc.execute("""
                        CREATE TABLE sys_role(id BIGINT,deleted BIGINT,frozen INTEGER,tenant_id BIGINT);
                        CREATE TABLE sys_roleuser_ref(role_id BIGINT,user_id BIGINT);
                        CREATE TABLE sys_roleres_ref(role_id BIGINT,resource_id BIGINT);
                        CREATE TABLE sys_tenant_resource(tenant_id BIGINT,resource_id BIGINT);
                        CREATE TABLE sys_module(id BIGINT,sort DOUBLE PRECISION,name TEXT,pid BIGINT,path TEXT,
                            isleaf BOOLEAN,code TEXT,icon TEXT,route_url TEXT,component_url TEXT,
                            deleted BIGINT,frozen INTEGER,tenant_id BIGINT,create_id BIGINT,modify_id BIGINT);
                        CREATE TABLE sys_module_resources(id BIGINT,main_id BIGINT,code TEXT,resource_desc TEXT,
                            resource_url TEXT,resource_type TEXT,sort DOUBLE PRECISION,deleted BIGINT,tenant_id BIGINT);
                        INSERT INTO sys_role VALUES(1,0,0,0);
                        INSERT INTO sys_roleuser_ref VALUES(1,9);
                        INSERT INTO sys_roleres_ref VALUES(1,11);
                        INSERT INTO sys_module VALUES(1,0,'Organization',0,'/1',true,'sys.organization',NULL,
                            '/sys/organization',NULL,0,0,0,NULL,88);
                        INSERT INTO sys_module_resources VALUES
                            (11,1,'read','Read','/sys/organization/list','GET',0,0,0),
                            (12,1,'write','Write','/sys/organization/edit','POST',1,0,0);
                        """);
                MybatisConfiguration mybatis = new MybatisConfiguration();
                mybatis.setEnvironment(new Environment("synthetic-menu",
                        new SpringManagedTransactionFactory(), dataSource));
                mybatis.setCacheEnabled(false);
                mybatis.getTypeAliasRegistry().registerAlias("module", ModuleEntity.class);
                mybatis.addInterceptor(new MybatisPlusConfig(config).mybatisPlusInterceptor());
                mybatis.addInterceptor(capture);
                for (String resource : List.of("mapper/common.xml", "mapper/Module.xml", "mapper/AuthorizedMenu.xml")) {
                    try (var stream = Resources.getResourceAsStream(resource)) {
                        new XMLMapperBuilder(stream, mybatis, resource, mybatis.getSqlFragments()).parse();
                    }
                }
                SqlSessionTemplate sessions = new SqlSessionTemplate(
                        new MybatisSqlSessionFactoryBuilder().build(mybatis));
                assertEquals("org.pkaq.sys.module.mapper.AuthorizedMenuMapper.selectMenuRows",
                        mybatis.getMappedStatement(AuthorizedMenuMapper.class.getName() + ".selectMenuRows").getId());
                oldMapper = sessions.getMapper(ModuleMapper.class);
                // 测试resolver仅映射本fixture创建的两个租户，不读取应用租户或连接配置。
                router = new TenantSchemaRouter(dataSource, config, tenantId -> {
                    if (tenantId != 101L && tenantId != 102L) {
                        throw new SecurityException("合成租户未获准");
                    }
                    return "tenant_" + tenantId;
                });
                service = new AuthorizedMenuService(sessions.getMapper(AuthorizedMenuMapper.class), config,
                        router, new CoreSchemaExecutor(jdbc, config), jdbc);
                transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
                transaction.setReadOnly(true);
            } catch (Exception | Error failure) {
                try {
                    postgres.close();
                } catch (Exception | Error closeFailure) {
                    failure.addSuppressed(closeFailure);
                }
                throw failure;
            }
        }

        private void withUser(long tenantId, String scope, Runnable action) {
            ThreadUser user = new ThreadUser().setUserId(9L).setTenantId(tenantId).setDeptId(0L)
                    .setDataScopes(List.of(new ThreadUser.DataScope(scope, List.of())));
            ThreadUserHelper.runWithUser(user, () -> transaction.executeWithoutResult(status -> action.run()));
        }

        @Override
        public void close() throws Exception {
            TenantContext.clear();
            postgres.close();
        }
    }

    @Intercepts({
            @Signature(type = Executor.class, method = "query",
                    args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
            @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
    })
    private static final class SqlCapture implements Interceptor {
        private final List<String> queries = new ArrayList<>();
        private final List<String> preparedStatementIds = new ArrayList<>();
        private String executingStatementId;
        private Runnable beforeMenuPrepare;

        /** 记录合成测试实际准备的SQL，不读取运行实例或应用日志。 */
        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            if (invocation.getTarget() instanceof Executor) {
                String previous = executingStatementId;
                executingStatementId = ((MappedStatement) invocation.getArgs()[0]).getId();
                try {
                    return invocation.proceed();
                } finally {
                    executingStatementId = previous;
                }
            }
            preparedStatementIds.add(executingStatementId);
            queries.add(((StatementHandler) invocation.getTarget()).getBoundSql().getSql());
            if ((AuthorizedMenuMapper.class.getName() + ".selectMenuRows").equals(executingStatementId)
                    && null != beforeMenuPrepare) {
                beforeMenuPrepare.run();
            }
            return invocation.proceed();
        }
    }
}
