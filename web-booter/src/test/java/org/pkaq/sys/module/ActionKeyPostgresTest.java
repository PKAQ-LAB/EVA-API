package org.pkaq.sys.module;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.mybatis.tenant.TenantSchemaRouter;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.sys.module.entity.ModuleResources;
import org.pkaq.sys.module.mapper.AuthorizedMenuMapper;
import org.pkaq.sys.module.mapper.ModuleResourceMapper;
import org.pkaq.sys.module.service.AuthorizedMenuService;
import org.pkaq.sys.module.vo.ModuleMenuVo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 一个自有合成PG验证新库动作种子、真实菜单投影与NULL更新，不使用应用数据源。
 * @author PKAQ
 * @date 2026-11-01
 */
class ActionKeyPostgresTest {
    @Test
    void initializesActionRowsAndKeepsGrantedKeysSeparateFromHttpMetadata() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder()
                .setServerConfig("listen_addresses", "127.0.0.1").setPort(0).start()) {
            Flyway flyway = Flyway.configure().dataSource(postgres.getPostgresDatabase())
                    .schemas("eva").defaultSchema("eva").locations("classpath:db/migration").load();
            assertEquals(1, flyway.migrate().migrationsExecuted);
            assertEquals(0, flyway.migrate().migrationsExecuted);
            assertTrue(flyway.validateWithResult().validationSuccessful);
            DataSource source = new AbstractDataSource() {
                @Override
                public Connection getConnection() throws SQLException {
                    Connection connection = postgres.getPostgresDatabase().getConnection();
                    connection.setSchema("eva");
                    return connection;
                }

                @Override
                public Connection getConnection(String username, String password) throws SQLException {
                    return getConnection();
                }
            };
            JdbcTemplate jdbc = new JdbcTemplate(source);
            assertEquals("127.0.0.1", jdbc.queryForObject("SELECT host(inet_server_addr())", String.class));
            assertEquals(78, jdbc.queryForObject("SELECT count(*) FROM sys_module_resources "
                    + "WHERE id BETWEEN 1000000000000060001 AND 1000000000000060078", Integer.class));
            assertEquals(72, jdbc.queryForObject("SELECT count(*) FROM sys_module_resources "
                    + "WHERE action_key IS NOT NULL", Integer.class));
            assertEquals(78, jdbc.queryForObject("SELECT count(*) FROM sys_roleres_ref "
                    + "WHERE role_id=1000000000000000004 AND resource_id BETWEEN "
                    + "1000000000000060001 AND 1000000000000060078", Integer.class));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM sys_module_resources "
                    + "WHERE id BETWEEN 1000000000000001401 AND 1000000000000001416 "
                    + "AND action_key IS NOT NULL", Integer.class));
            MybatisConfiguration mybatis = new MybatisConfiguration();
            mybatis.setMapUnderscoreToCamelCase(true);
            mybatis.setEnvironment(new Environment("action-key", new SpringManagedTransactionFactory(), source));
            mybatis.addMapper(ModuleResourceMapper.class);
            try (var stream = Resources.getResourceAsStream("mapper/AuthorizedMenu.xml")) {
                new XMLMapperBuilder(stream, mybatis, "mapper/AuthorizedMenu.xml", mybatis.getSqlFragments()).parse();
            }
            SqlSessionTemplate sessions = new SqlSessionTemplate(new MybatisSqlSessionFactoryBuilder().build(mybatis));
            EvaConfig config = new EvaConfig();
            config.setMode("standalone");
            config.getTenant().setEnable(false);
            config.getTenant().setStandaloneSchema("eva");
            AuthorizedMenuService service = new AuthorizedMenuService(sessions.getMapper(AuthorizedMenuMapper.class),
                    config, new TenantSchemaRouter(source, config, tenant -> "eva"),
                    new CoreSchemaExecutor(jdbc, config), jdbc);
            TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(source));
            transaction.setReadOnly(true);
            ThreadUser actor = new ThreadUser().setUserId(1000000000000000005L).setTenantId(0L);
            ThreadUserHelper.runWithUser(actor, () -> transaction.executeWithoutResult(status -> {
                ModuleMenuVo role = find(service.fetchCurrentMenus(), "sys.role");
                assertTrue(role.getResources().containsAll(List.of("list", "read", "create", "update", "delete")));
                assertEquals(role.getResources().size(), role.getResources().stream().distinct().count());
            }));
            transaction.setReadOnly(false);
            ThreadUserHelper.runWithUser(actor, () -> transaction.executeWithoutResult(status -> {
                ModuleResources resource = new ModuleResources();
                resource.setId(1000000000000060001L);
                resource.setActionKey(null);
                assertEquals(1, sessions.getMapper(ModuleResourceMapper.class).updateById(resource));
            }));
            assertNull(jdbc.queryForObject("SELECT action_key FROM sys_module_resources "
                    + "WHERE id=1000000000000060001", String.class));
            assertEquals("/sys/account/list", jdbc.queryForObject("SELECT resource_url FROM sys_module_resources "
                    + "WHERE id=1000000000000060001", String.class));
            ThreadUserHelper.runWithUser(actor, () -> transaction.executeWithoutResult(status -> {
                for (String action : List.of("create", "update")) {
                    ModuleResources resource = new ModuleResources();
                    resource.setMainId(1000000000000000101L);
                    resource.setTenantId(0L);
                    resource.setActionKey(action);
                    resource.setResourceUrl("/synthetic/same-http");
                    resource.setResourceType("POST");
                    assertEquals(1, sessions.getMapper(ModuleResourceMapper.class).insert(resource));
                    assertNull(resource.getCode());
                }
            }));
            assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM sys_module_resources "
                    + "WHERE main_id=1000000000000000101 AND code IS NULL "
                    + "AND resource_url='/synthetic/same-http' AND action_key IN ('create','update')", Integer.class));
        }
    }

    private ModuleMenuVo find(Collection<ModuleMenuVo> menus, String code) {
        for (ModuleMenuVo menu : menus) {
            if (code.equals(menu.getCode())) {
                return menu;
            }
            if (menu.getChildren() != null) {
                ModuleMenuVo nested = find(menu.getChildren(), code);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }
}
