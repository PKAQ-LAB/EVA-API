package org.pkaq.sys.role;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.pkaq.config.MybatisPlusDataPermissionHandler;
import org.pkaq.core.auth.adapter.mybatis.role.service.MybatisPostEligibilityQuery;
import org.pkaq.core.mybatis.permission.PostReadScope;
import org.pkaq.core.mybatis.permission.PostScopeDataPermissionInterceptor;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.TenantProperties;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 一个隔离PG验证真实岗位来源与真实MP同statement在读写用途中的不同贡献。
 * @author PKAQ
 * @date 2026-11-01
 */
public class PostScopePostgresTest {
    @TableName("scope_record")
    public static class RecordRow {
        @TableId
        private Long id;
        private Long createId;
        private Long modifyId;
        private String label;

        /** 获取测试记录ID。 */
        public Long getId() {
            return id;
        }

        /** 获取测试记录标签。 */
        public String getLabel() {
            return label;
        }
    }

    public interface Records extends BaseMapper<RecordRow> {
    }

    @Test
    void qualifiedEligibilityAndRealMapperReadWriteIsolation() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().setServerConfig("listen_addresses", "127.0.0.1")
                .setPort(0).start()) {
            DataSource root = postgres.getPostgresDatabase();
            JdbcTemplate bootstrap = new JdbcTemplate(root);
            bootstrap.execute("CREATE SCHEMA post_a");
            bootstrap.execute("CREATE SCHEMA post_b");
            for (String schema : List.of("post_a", "post_b")) {
                JdbcTemplate db = new JdbcTemplate(source(root, schema));
                db.execute("CREATE TABLE sys_account(id bigint,deleted bigint,frozen int,tenant_id bigint)");
                db.execute("CREATE TABLE sys_role(id bigint,deleted bigint,frozen int,tenant_id bigint,"
                        + "data_scope text,data_post_ids text)");
                db.execute("CREATE TABLE sys_roleuser_ref(role_id bigint,user_id bigint)");
                db.execute("CREATE TABLE sys_post(id bigint,deleted bigint,frozen int,tenant_id bigint)");
                db.execute("CREATE TABLE sys_postuser_ref(post_id bigint,user_id bigint)");
                db.execute("INSERT INTO sys_account VALUES(7,0,0,0)");
                db.execute("INSERT INTO sys_role VALUES(31,0,0,0,'0006','101,102')");
                db.execute("INSERT INTO sys_roleuser_ref VALUES(31,7)");
                db.execute("INSERT INTO sys_post VALUES(101,0,0,0),(102,0,9999,0),(103,0,0,0)");
                db.execute("INSERT INTO sys_postuser_ref(post_id,user_id) VALUES(101,7),(102,7)");
            }
            DataSource data = source(root, "post_a");
            JdbcTemplate db = new JdbcTemplate(data);
            db.execute("ALTER TABLE sys_postuser_ref ADD tenant_id bigint DEFAULT 0");
            EvaConfig config = new EvaConfig();
            config.getTenant().setEnable(false);
            config.getTenant().setStandaloneSchema("post_a");
            config.getDataPermission().setEnable(true);
            MybatisPostEligibilityQuery eligibility = new MybatisPostEligibilityQuery(db, config);
            assertEquals(Map.of(31L, List.of(101L, 102L)), eligibility.find(7L));
            db.execute("UPDATE sys_postuser_ref SET tenant_id=99");
            assertTrue(eligibility.find(7L).isEmpty());
            db.execute("UPDATE sys_postuser_ref SET tenant_id=NULL");
            assertTrue(eligibility.find(7L).containsKey(31L));
            db.execute("UPDATE sys_postuser_ref SET tenant_id=0");
            assertTrue(eligibility.find(7L).containsKey(31L));
            db.execute("UPDATE sys_post SET frozen=1 WHERE id=101");
            assertTrue(eligibility.find(7L).containsKey(31L));
            db.execute("UPDATE sys_post SET frozen=1 WHERE id=102");
            assertTrue(eligibility.find(7L).isEmpty());
            db.execute("UPDATE sys_post SET frozen=0,deleted=1");
            assertTrue(eligibility.find(7L).isEmpty());
            db.execute("UPDATE sys_post SET deleted=0");
            db.execute("UPDATE sys_role SET deleted=1");
            assertTrue(eligibility.find(7L).isEmpty());
            db.execute("UPDATE sys_role SET deleted=0,data_post_ids='101,broken'");
            assertTrue(eligibility.find(7L).isEmpty());
            db.execute("UPDATE sys_role SET data_post_ids='101,102'");
            db.execute("UPDATE sys_post SET tenant_id=99");
            assertTrue(eligibility.find(7L).isEmpty());
            db.execute("UPDATE sys_post SET tenant_id=0");
            db.execute("DELETE FROM sys_postuser_ref");
            db.execute("INSERT INTO sys_postuser_ref(post_id,user_id) VALUES(103,7)");
            assertTrue(eligibility.find(7L).isEmpty());
            db.execute("INSERT INTO sys_postuser_ref(post_id,user_id) VALUES(101,7)");
            assertTrue(eligibility.find(7L).containsKey(31L));
            verifyPrivateSource(root, config);
            config.setMode("standalone");
            config.getTenant().setEnable(false);
            config.getTenant().setMode(null);
            verifyMapper(data, config);
            verifyPostVersions(data);
            verifyInitialization(root);
        }
    }

    private void verifyPrivateSource(DataSource root, EvaConfig config) {
        config.setMode("saas");
        config.getTenant().setEnable(true);
        config.getTenant().setMode(TenantProperties.MODE_SCHEMA);
        TenantContext.bind(9L, "post_b");
        try {
            JdbcTemplate other = new JdbcTemplate(source(root, "post_b"));
            assertEquals(0, other.queryForObject("SELECT count(*) FROM information_schema.columns"
                    + " WHERE table_schema='post_b' AND table_name='sys_postuser_ref' AND column_name='tenant_id'",
                    Integer.class));
            assertTrue(new MybatisPostEligibilityQuery(other, config).find(7L).containsKey(31L));
            other.execute("UPDATE sys_post SET frozen=1");
            assertTrue(new MybatisPostEligibilityQuery(other, config).find(7L).isEmpty());
            assertTrue(new MybatisPostEligibilityQuery(new JdbcTemplate(source(root, "post_a")), config)
                    .find(7L).isEmpty());
            other.execute("DROP TABLE sys_post");
            assertThrows(RuntimeException.class, () -> new MybatisPostEligibilityQuery(other, config).find(7L));
        } finally {
            TenantContext.clear();
        }
    }

    /** 同一合成库验证岗位级联与版本推进的事务原子性。 */
    private void verifyPostVersions(DataSource data) {
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                new org.apache.ibatis.builder.MapperBuilderAssistant(new MybatisConfiguration(), "post-fixture"),
                org.pkaq.sys.post.entity.PostEntity.class);
        JdbcTemplate db = new JdbcTemplate(data);
        db.execute("ALTER TABLE sys_account ADD perm_ver bigint DEFAULT 0");
        db.execute("ALTER TABLE sys_post ADD path text, ADD pid bigint");
        db.execute("UPDATE sys_post SET frozen=0,path='/'||id,pid=0");
        db.execute("UPDATE sys_post SET path='/101/102',pid=101 WHERE id=102");
        db.execute("INSERT INTO sys_postuser_ref(post_id,user_id) VALUES(102,7)");
        MybatisConfiguration actual = new MybatisConfiguration();
        actual.setEnvironment(new Environment("post-members", new SpringManagedTransactionFactory(), data));
        actual.addMapper(org.pkaq.sys.post.mapper.PostUserMapper.class);
        try (var session = new MybatisSqlSessionFactoryBuilder().build(actual).openSession()) {
            assertEquals(List.of(7L), session.getMapper(org.pkaq.sys.post.mapper.PostUserMapper.class)
                    .selectCascadeUserIds(101L, "/101", 1));
        }
        var postMapper = org.mockito.Mockito.mock(org.pkaq.sys.post.mapper.PostMapper.class);
        var refs = org.mockito.Mockito.mock(org.pkaq.sys.post.mapper.PostUserMapper.class);
        var users = org.mockito.Mockito.mock(org.pkaq.sys.user.mapper.UserMapper.class);
        org.mockito.Mockito.when(postMapper.selectById(101L)).thenAnswer(call -> {
            var post = new org.pkaq.sys.post.entity.PostEntity();
            post.setId(101L);
            post.setPid(0L);
            post.setPath("/101");
            post.setFrozen(db.queryForObject("SELECT frozen FROM sys_post WHERE id=101", Integer.class) == 1
                    ? org.pkaq.core.enums.FrozenEnumm.FROZEN : org.pkaq.core.enums.FrozenEnumm.UN_FROZEN);
            return post;
        });
        org.mockito.Mockito.when(refs.selectCascadeUserIds(org.mockito.ArgumentMatchers.eq(101L),
                org.mockito.ArgumentMatchers.eq("/101"), org.mockito.ArgumentMatchers.anyInt())).thenAnswer(call ->
                db.queryForList("SELECT DISTINCT ref.user_id FROM sys_postuser_ref ref JOIN sys_post p"
                        + " ON p.id=ref.post_id WHERE p.deleted=0 AND (p.id=101 OR p.path LIKE '/101/%')"
                        + " AND p.frozen<>?", Long.class, (Object) call.getArgument(2)));
        org.mockito.Mockito.doAnswer(call -> db.update("UPDATE sys_post SET frozen=? WHERE deleted=0"
                + " AND (id=101 OR path LIKE '/101/%')", (Object) call.getArgument(2)))
                .when(postMapper).cascadeFrozen(org.mockito.ArgumentMatchers.eq(101L),
                        org.mockito.ArgumentMatchers.eq("/101"), org.mockito.ArgumentMatchers.anyInt());
        java.util.concurrent.atomic.AtomicBoolean fail = new java.util.concurrent.atomic.AtomicBoolean();
        org.mockito.Mockito.when(users.incrementPermVer(7L)).thenAnswer(call -> {
            int count = db.update("UPDATE sys_account SET perm_ver=perm_ver+1 WHERE id=7");
            if (fail.get()) {
                throw new IllegalStateException("合成版本更新失败");
            }
            return count;
        });
        var service = new org.pkaq.sys.post.service.PostService(
                org.mockito.Mockito.mock(org.pkaq.sys.post.convert.PostConvert.class), refs, users);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "mapper", postMapper);
        var ids = new org.pkaq.core.mvc.bo.SingleArray<Long>();
        ids.setParam(java.util.Set.of(101L));
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(data));
        transaction.executeWithoutResult(status -> service.switchFrozen(ids));
        assertEquals(1L, db.queryForObject("SELECT perm_ver FROM sys_account WHERE id=7", Long.class));
        assertEquals(2, db.queryForObject(
                "SELECT count(*) FROM sys_post WHERE id IN(101,102) AND frozen=1", Integer.class));
        var member = new org.pkaq.sys.post.entity.PostUserEntity();
        member.setUserId(7L);
        member.setPostId(103L);
        org.mockito.Mockito.when(refs.selectList(org.mockito.ArgumentMatchers.any(
                com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(member));
        org.mockito.Mockito.when(postMapper.delete(org.mockito.ArgumentMatchers.any(
                com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenAnswer(call ->
                db.update("UPDATE sys_post SET deleted=99 WHERE id=103"));
        org.mockito.Mockito.when(refs.delete(org.mockito.ArgumentMatchers.any(
                com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenAnswer(call ->
                db.update("DELETE FROM sys_postuser_ref WHERE post_id=103"));
        fail.set(true);
        assertThrows(IllegalStateException.class,
                () -> transaction.executeWithoutResult(status -> service.del(java.util.Set.of(103L))));
        assertEquals(0L, db.queryForObject("SELECT deleted FROM sys_post WHERE id=103", Long.class));
        assertEquals(1, db.queryForObject("SELECT count(*) FROM sys_postuser_ref WHERE post_id=103", Integer.class));
        fail.set(false);
        transaction.executeWithoutResult(status -> service.del(java.util.Set.of(103L)));
        assertEquals(2L, db.queryForObject("SELECT perm_ver FROM sys_account WHERE id=7", Long.class));
        assertEquals(99L, db.queryForObject("SELECT deleted FROM sys_post WHERE id=103", Long.class));
        assertEquals(0, db.queryForObject("SELECT count(*) FROM sys_postuser_ref WHERE post_id=103", Integer.class));
        fail.set(true);
        assertThrows(IllegalStateException.class,
                () -> transaction.executeWithoutResult(status -> service.switchFrozen(ids)));
        assertEquals(2L, db.queryForObject("SELECT perm_ver FROM sys_account WHERE id=7", Long.class));
        assertEquals(2, db.queryForObject(
                "SELECT count(*) FROM sys_post WHERE id IN(101,102) AND frozen=1", Integer.class));
    }

    /** 只在本合成PG执行获批的两份初始化，现库迁移不在本用例内。 */
    private void verifyInitialization(DataSource data) throws Exception {
        var resources = java.nio.file.Path.of("web-booter/src/main/resources").toAbsolutePath();
        var flyway = org.flywaydb.core.Flyway.configure().dataSource(data).schemas("eva").defaultSchema("eva")
                .locations("filesystem:" + resources.resolve("db/migration"))
                .baselineOnMigrate(false).cleanDisabled(true).load();
        assertEquals(1, flyway.migrate().migrationsExecuted);
        flyway.validate();
        assertEquals(0, flyway.migrate().migrationsExecuted);
        JdbcTemplate db = new JdbcTemplate(data);
        assertEquals("text", db.queryForObject("SELECT data_type FROM information_schema.columns"
                + " WHERE table_schema='eva' AND table_name='sys_role' AND column_name='data_post_ids'", String.class));
        assertEquals(List.of("0000", "0001", "0002", "0003", "0006", "0007"), db.queryForList(
                "SELECT d_code FROM eva.sys_dict_item WHERE main_id=1000000000000004001 ORDER BY d_code",
                String.class));
        assertEquals("指定岗位", db.queryForObject("SELECT d_value FROM eva.sys_dict_item"
                + " WHERE id=1000000000000004008 AND main_id=1000000000000004001 AND sort=7", String.class));
        db.execute("CREATE SCHEMA init_private");
        try (Connection connection = data.getConnection()) {
            connection.setSchema("init_private");
            org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(connection,
                    new org.springframework.core.io.FileSystemResource(resources.resolve(
                            "db/tenant-migration/V1__TENANT_SCHEMA_BASE.sql")));
        }
        assertEquals("text", db.queryForObject("SELECT data_type FROM information_schema.columns"
                + " WHERE table_schema='init_private' AND table_name='sys_role' AND column_name='data_post_ids'",
                String.class));
    }

    private void verifyMapper(DataSource data, EvaConfig config) {
        JdbcTemplate db = new JdbcTemplate(data);
        db.execute("CREATE TABLE scope_record(id bigint PRIMARY KEY,create_id bigint,modify_id bigint,label text)");
        db.execute("INSERT INTO scope_record VALUES(1,7,7,'owned'),(2,8,8,'other')");
        MybatisConfiguration mybatis = new MybatisConfiguration();
        mybatis.setEnvironment(new Environment("post-scope", new SpringManagedTransactionFactory(), data));
        MybatisPlusInterceptor plugins = new MybatisPlusInterceptor();
        plugins.addInnerInterceptor(new PostScopeDataPermissionInterceptor(
                new MybatisPlusDataPermissionHandler(config)));
        plugins.addInnerInterceptor(new PaginationInnerInterceptor());
        mybatis.addInterceptor(plugins);
        mybatis.addMapper(Records.class);
        SqlSessionFactory factory = new MybatisSqlSessionFactoryBuilder().build(mybatis);
        ThreadUser user = new ThreadUser().setUserId(7L).setTenantId(0L).setDataScopes(
                List.of(new ThreadUser.DataScope("0006", List.of(), List.of(101L), true)));
        ThreadUserHelper.runWithUser(user, () -> {
            transaction(data, factory, true, mapper -> assertNull(mapper.selectById(2L)));
            try (PostReadScope.Window ignored = PostReadScope.read()) {
                transaction(data, factory, true, mapper -> {
                    assertNotNull(mapper.selectById(2L));
                    assertEquals(2L, mapper.selectCount(null));
                    assertEquals(2L, mapper.selectPage(new Page<RecordRow>(1, 1), null).getTotal());
                });
                transaction(data, factory, false, mapper -> {
                    assertNull(mapper.selectById(2L));
                    assertEquals(0, mapper.update(null, Wrappers.<RecordRow>lambdaUpdate()
                            .eq(RecordRow::getId, 2L).set(RecordRow::getLabel, "forbidden")));
                    assertEquals(0, mapper.deleteById(2L));
                });
            }
            user.setDataScopes(List.of(new ThreadUser.DataScope("0006", List.of(), List.of(101L), true),
                    new ThreadUser.DataScope("0007", List.of())));
            transaction(data, factory, false, mapper -> {
                assertEquals(1, mapper.update(null, Wrappers.<RecordRow>lambdaUpdate()
                        .eq(RecordRow::getId, 1L).set(RecordRow::getLabel, "allowed")));
                assertNull(mapper.selectById(2L));
            });
        });
        assertEquals("other", db.queryForObject("SELECT label FROM scope_record WHERE id=2", String.class));
    }

    private void transaction(DataSource data, SqlSessionFactory factory, boolean readOnly, Consumer<Records> work) {
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(data));
        transaction.setReadOnly(readOnly);
        transaction.executeWithoutResult(status -> {
            try (var session = factory.openSession()) {
                work.accept(session.getMapper(Records.class));
            }
        });
    }

    private DataSource source(DataSource original, String schema) {
        return new AbstractDataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                Connection connection = original.getConnection();
                connection.setSchema(schema);
                return connection;
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return getConnection();
            }
        };
    }
}
