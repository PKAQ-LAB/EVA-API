package org.pkaq.sys.user;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.handlers.CompositeEnumTypeHandler;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.adapter.mybatis.user.entity.AuthUserEntity;
import org.pkaq.core.auth.adapter.mybatis.user.mapper.AuthUserMapper;
import org.pkaq.core.mybatis.enums.UniversalEnumTypeHandler;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.user.bo.UserQueryBo;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.entity.AccountProfileEntity;
import org.pkaq.sys.user.mapper.AccountProfileMapper;
import org.pkaq.sys.user.mapper.UserMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 不启动应用，用实际 Mapper XML 与隔离 PostgreSQL 验证可选资料查询。
 *
 * @author Codex
 * @date 2026-10-08
 */
class UserAccountProfileMapperPostgresTest {

    /** 详情与列表必须加载档案，同时保留没有档案的账号。 */
    @Test
    void readsOptionalProfilesWithActualMapper() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            JdbcTemplate database = new JdbcTemplate(postgres.getPostgresDatabase());
            database.execute("CREATE TABLE SYS_ACCOUNT(ID BIGINT PRIMARY KEY, ACCOUNT TEXT, "
                    + "DELETED BIGINT, UTC_MODIFY TIMESTAMP)");
            database.execute("CREATE TABLE SYS_ACCOUNT_PROFILE(ACCOUNT_ID BIGINT PRIMARY KEY, "
                    + "CODE TEXT, NAME TEXT, DEPT_ID BIGINT)");
            database.update("INSERT INTO SYS_ACCOUNT VALUES(1,'manager',0,NOW()),(2,'account-only',0,NOW()),"
                    + "(3,'deleted',3,NOW())");
            database.update("INSERT INTO SYS_ACCOUNT_PROFILE VALUES(1,'E1','管理用户',7)");
            MybatisConfiguration configuration = new MybatisConfiguration();
            configuration.setMapUnderscoreToCamelCase(true);
            configuration.setEnvironment(new Environment("isolated-postgres", new JdbcTransactionFactory(),
                    postgres.getPostgresDatabase()));
            configuration.getTypeAliasRegistry().registerAlias("user", UserEntity.class);
            configuration.getTypeAliasRegistry().registerAlias("role", RoleEntity.class);
            configuration.getTypeAliasRegistry().registerAlias("module", ModuleEntity.class);
            for (String resource : List.of("mapper/common.xml", "mapper/User.xml")) {
                try (var input = getClass().getClassLoader().getResourceAsStream(resource)) {
                    new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
                }
            }
            configuration.addMapper(AccountProfileMapper.class);
            try (SqlSession session = new MybatisSqlSessionFactoryBuilder().build(configuration).openSession()) {
                UserMapper mapper = session.getMapper(UserMapper.class);
                UserEntity managed = mapper.selectById(1L);
                assertEquals("E1", managed.getCode());
                assertEquals("管理用户", managed.getName());
                assertEquals(7L, managed.getDeptId());
                assertEquals("account-only", mapper.selectById(2L).getAccount());
                assertNull(mapper.selectById(2L).getDeptId());
                assertNull(mapper.selectById(3L));
                assertEquals(2, mapper.selectManagedList(new UserQueryBo(), List.of()).size());
                assertEquals(1L, mapper.countDuplicate(null, "E1", null));
                assertEquals(1L, mapper.countDepartmentAccounts(java.util.Set.of(7L)));
                UserQueryBo emptyQuery = new UserQueryBo();
                emptyQuery.setAccount("");
                emptyQuery.setCode("");
                emptyQuery.setEmail("");
                emptyQuery.setName("");
                emptyQuery.setTel("");
                assertEquals(2, mapper.selectManagedList(emptyQuery, List.of()).size());
                AccountProfileEntity cleared = new AccountProfileEntity();
                cleared.setAccountId(1L);
                assertEquals(1, session.getMapper(AccountProfileMapper.class).updateById(cleared));
                session.clearCache();
                assertNull(mapper.selectById(1L).getDeptId());
                assertNull(mapper.selectById(1L).getCode());
                assertNull(mapper.selectById(1L).getName());
            }
        }
    }

    /** 纯认证查询只需账号表，不依赖平台租户、管理资料或角色表。 */
    @Test
    void authenticatesWithOnlyAccountTable() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            // 与应用 MybatisPlusConfig 的真实枚举映射配置保持一致。
            CompositeEnumTypeHandler.setDefaultEnumTypeHandler(UniversalEnumTypeHandler.class);
            JdbcTemplate database = new JdbcTemplate(postgres.getPostgresDatabase());
            database.execute("CREATE TABLE SYS_ACCOUNT(ID BIGINT PRIMARY KEY, ACCOUNT TEXT, PASSWORD TEXT, "
                    + "NICK_NAME TEXT, FROZEN INTEGER, PERM_VER BIGINT, DELETED BIGINT, TEL TEXT, EMAIL TEXT)");
            database.update("INSERT INTO SYS_ACCOUNT VALUES(11,'only-account','preserved-hash','昵称',0,9,0,"
                    + "'00000000000','sample@example.test')");
            MybatisConfiguration configuration = new MybatisConfiguration();
            configuration.setMapUnderscoreToCamelCase(true);
            configuration.setEnvironment(new Environment("isolated-auth", new JdbcTransactionFactory(),
                    postgres.getPostgresDatabase()));
            configuration.getTypeAliasRegistry().registerAlias("authUser", AuthUserEntity.class);
            try (var input = getClass().getClassLoader().getResourceAsStream("mapper/AuthUser.xml")) {
                new XMLMapperBuilder(input, configuration, "mapper/AuthUser.xml",
                        configuration.getSqlFragments()).parse();
            }
            try (SqlSession session = new MybatisSqlSessionFactoryBuilder().build(configuration).openSession()) {
                AuthUserMapper mapper = session.getMapper(AuthUserMapper.class);
                AuthUserEntity query = new AuthUserEntity();
                query.setAccount("only-account");
                AuthUserEntity account = mapper.getUserAccount(query);
                assertEquals(11L, account.getId());
                assertEquals("preserved-hash", account.getPassword());
                assertEquals(0L, account.getTenantId());
                assertEquals(9L, mapper.getAuthState(11L).getPermVer());
                assertEquals(11L, mapper.getTenantUserAccount(query).getId());
                assertEquals(9L, mapper.getTenantAuthState(11L).getPermVer());
            }
            assertEquals(1, database.queryForObject("SELECT COUNT(*) FROM information_schema.tables "
                    + "WHERE table_schema='public'", Integer.class));
        }
    }
}
