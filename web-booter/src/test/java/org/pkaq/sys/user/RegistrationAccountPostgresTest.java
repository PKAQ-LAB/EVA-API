package org.pkaq.sys.user;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.handlers.CompositeEnumTypeHandler;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.pkaq.core.account.AccountCreationCommand;
import org.pkaq.core.account.AccountCreationException;
import org.pkaq.core.auth.adapter.mybatis.user.entity.AuthUserEntity;
import org.pkaq.core.auth.adapter.mybatis.user.mapper.AuthUserMapper;
import org.pkaq.core.auth.adapter.mybatis.user.service.AuthUserService;
import org.pkaq.core.auth.adapter.mybatis.user.service.MybatisAccountRegistration;
import org.pkaq.core.auth.authentication.bo.RegistrationBo;
import org.pkaq.core.auth.authentication.ctrl.RegistrationCtrl;
import org.pkaq.core.auth.authentication.domain.JwtUserDetail;
import org.pkaq.core.auth.authentication.provider.LoginAuthenticationProvider;
import org.pkaq.core.auth.authentication.service.AccountRegistrationService;
import org.pkaq.core.auth.authentication.service.JwtUserDetailsService;
import org.pkaq.core.auth.authorization.service.AuthPermissionContextService;
import org.pkaq.core.auth.spi.IAccountProfileQuery;
import org.pkaq.core.auth.spi.IPermissionSnapshotQuery;
import org.pkaq.core.auth.spi.ITenantAuthRouter;
import org.pkaq.core.mybatis.account.entity.AccountEntity;
import org.pkaq.core.mybatis.account.mapper.AccountMapper;
import org.pkaq.core.mybatis.account.service.AccountCreationService;
import org.pkaq.core.mybatis.enums.UniversalEnumTypeHandler;
import org.pkaq.core.properties.Auth;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.util.json.JsonUtil;
import org.pkaq.config.MybatisPlusDataPermissionHandler;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import net.sf.jsqlparser.schema.Table;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 在隔离 PostgreSQL 上验证注册写入、并发唯一性及既有 MD5 登录协议兼容。
 *
 * @author Codex
 * @date 2026-10-08
 */
class RegistrationAccountPostgresTest {
    private static final String RAW_PASSWORD = "TestPassword123!";

    /** 实际注册控制器写账号表后，以既有前端摘要通过真实认证提供器登录。 */
    @Test
    void registersAccountThenAuthenticatesWithoutManagementTables() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            JdbcTemplate database = createDatabase(postgres);
            SqlSessionTemplate sessions = sessions(postgres);
            EvaConfig config = registrationConfig();
            AccountRegistrationService service = new AccountRegistrationService(config,
                    new MybatisAccountRegistration(new AccountCreationService(sessions.getMapper(AccountMapper.class))));
            var mvc = MockMvcBuilders.standaloneSetup(new RegistrationCtrl(service)).build();
            String response = mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"account\":\"fresh-account\",\"password\":\"" + RAW_PASSWORD
                            + "\",\"nickName\":\"新账号\"}"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertTrue(response.contains("\"success\":true"));
            assertFalse(response.toLowerCase().contains("token"));
            assertFalse(response.contains(RAW_PASSWORD));
            assertNull(SecurityContextHolder.getContext().getAuthentication());
            assertEquals(0, mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"account\":\"privilege-attempt\",\"password\":\"" + RAW_PASSWORD
                            + "\",\"roleIds\":[1],\"tenantId\":1,\"deptId\":2,\"frozen\":9999}"))
                    .andExpect(status().isBadRequest()).andReturn().getResponse().getCookies().length);
            assertEquals(1, tableCount(database));
            assertEquals(1, database.queryForObject("SELECT COUNT(*) FROM SYS_ACCOUNT", Integer.class));
            String hash = database.queryForObject("SELECT PASSWORD FROM SYS_ACCOUNT", String.class);
            String loginPassword = DigestUtils.md5DigestAsHex(RAW_PASSWORD.getBytes(StandardCharsets.UTF_8));
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            assertTrue(encoder.matches(loginPassword, hash));
            assertFalse(encoder.matches(RAW_PASSWORD, hash));
            IPermissionSnapshotQuery permissions = mock(IPermissionSnapshotQuery.class);
            IAccountProfileQuery profiles = mock(IAccountProfileQuery.class);
            ITenantAuthRouter router = mock(ITenantAuthRouter.class);
            AuthPermissionContextService context = new AuthPermissionContextService(config, permissions, router, profiles);
            JwtUserDetailsService details = new JwtUserDetailsService(
                    new AuthUserService(sessions.getMapper(AuthUserMapper.class), config), config, context);
            LoginAuthenticationProvider provider = new LoginAuthenticationProvider(details, encoder, router);
            var authenticated = provider.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated("fresh-account", loginPassword));
            assertTrue(authenticated.isAuthenticated());
            // Spring Security 7 自动附加密码因子凭证，不代表业务角色或启用多因素认证。
            assertEquals(java.util.Set.of(FactorGrantedAuthority.PASSWORD_AUTHORITY),
                    authenticated.getAuthorities().stream().map(authority -> authority.getAuthority())
                            .collect(java.util.stream.Collectors.toSet()));
            JwtUserDetail principal = (JwtUserDetail) authenticated.getPrincipal();
            assertTrue(principal.getAuthorities().isEmpty());
            assertNull(principal.getRoleIds());
            verifyNoInteractions(permissions, profiles, router);
            assertEquals(0L, database.queryForObject("SELECT PERM_VER FROM SYS_ACCOUNT", Long.class));
            config.getDataPermission().setEnable(true);
            Long accountId = database.queryForObject("SELECT ID FROM SYS_ACCOUNT", Long.class);
            ThreadUserHelper.runWithUser(new ThreadUser().setUserId(accountId)
                    .setDataScopes(java.util.List.of()), () -> assertEquals("1 = 0",
                    new MybatisPlusDataPermissionHandler(config).getSqlSegment(new Table("BIZ_ORDER"), null,
                            "example.Mapper.selectList").toString()));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /** 两个事务先同时读到账号不存在时，数据库索引仍只允许一个写入。 */
    @Test
    void uniqueIndexRejectsConcurrentDuplicateAccounts() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.start()) {
            JdbcTemplate database = createDatabase(postgres);
            AccountMapper actual = sessions(postgres).getMapper(AccountMapper.class);
            AccountMapper synchronizedMapper = mock(AccountMapper.class);
            CyclicBarrier barrier = new CyclicBarrier(2);
            when(synchronizedMapper.countActiveAccount(anyString())).thenAnswer(invocation -> {
                Long count = actual.countActiveAccount(invocation.getArgument(0));
                barrier.await(30, TimeUnit.SECONDS);
                return count;
            });
            when(synchronizedMapper.insert(any(AccountEntity.class))).thenAnswer(
                    invocation -> actual.insert(invocation.getArgument(0, AccountEntity.class)));
            AccountCreationService creation = new AccountCreationService(synchronizedMapper);
            try (var executor = Executors.newFixedThreadPool(2)) {
                var first = executor.submit(() -> createAttempt(creation));
                var second = executor.submit(() -> createAttempt(creation));
                assertEquals(1, first.get(45, TimeUnit.SECONDS) + second.get(45, TimeUnit.SECONDS));
            }
            assertEquals(1, database.queryForObject("SELECT COUNT(*) FROM SYS_ACCOUNT", Integer.class));
            assertEquals(1, tableCount(database));
        }
    }

    /** 请求和存储对象的默认日志字符串、JSON 不得泄露注册密码。 */
    @Test
    void excludesPasswordFromTransportSerializationAndLogStrings() {
        RegistrationBo request = new RegistrationBo();
        request.setAccount("redaction-account");
        request.setPassword(RAW_PASSWORD);
        AccountCreationCommand command = new AccountCreationCommand();
        command.setPassword(RAW_PASSWORD);
        AccountEntity entity = new AccountEntity();
        entity.setPassword(RAW_PASSWORD);
        assertFalse(request.toString().contains(RAW_PASSWORD));
        assertFalse(JsonUtil.toJson(request).contains(RAW_PASSWORD));
        assertFalse(command.toString().contains(RAW_PASSWORD));
        assertFalse(JsonUtil.toJson(command).contains(RAW_PASSWORD));
        assertFalse(entity.toString().contains(RAW_PASSWORD));
        assertFalse(JsonUtil.toJson(entity).contains(RAW_PASSWORD));
    }

    private int createAttempt(AccountCreationService creation) {
        AccountCreationCommand command = new AccountCreationCommand();
        command.setAccount("concurrent-account");
        command.setPassword("md5-contract-password");
        try {
            creation.create(command);
            return 1;
        } catch (AccountCreationException exception) {
            assertEquals(AccountCreationException.Reason.DUPLICATE_ACCOUNT, exception.getReason());
            return 0;
        }
    }

    private EvaConfig registrationConfig() {
        EvaConfig config = new EvaConfig();
        config.setMode("standalone");
        config.getAuth().setRegistration(new Auth.Registration());
        config.getAuth().getRegistration().setEnabled(true);
        config.getTenant().setEnable(false);
        config.getResourcePermission().setEnable(false);
        config.getDataPermission().setEnable(false);
        return config;
    }

    private SqlSessionTemplate sessions(EmbeddedPostgres postgres) throws Exception {
        CompositeEnumTypeHandler.setDefaultEnumTypeHandler(UniversalEnumTypeHandler.class);
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setEnvironment(new Environment("registration-isolated", new SpringManagedTransactionFactory(),
                postgres.getPostgresDatabase()));
        configuration.addMapper(AccountMapper.class);
        configuration.getTypeAliasRegistry().registerAlias("authUser", AuthUserEntity.class);
        try (var input = getClass().getClassLoader().getResourceAsStream("mapper/AuthUser.xml")) {
            new XMLMapperBuilder(input, configuration, "mapper/AuthUser.xml", configuration.getSqlFragments()).parse();
        }
        return new SqlSessionTemplate(new MybatisSqlSessionFactoryBuilder().build(configuration));
    }

    private JdbcTemplate createDatabase(EmbeddedPostgres postgres) {
        JdbcTemplate database = new JdbcTemplate(postgres.getPostgresDatabase());
        database.execute("CREATE TABLE SYS_ACCOUNT(ID BIGINT PRIMARY KEY, REVISION INTEGER, DELETED BIGINT DEFAULT 0, "
                + "FROZEN INTEGER DEFAULT 0, SORT DOUBLE PRECISION DEFAULT 0, CREATE_ID BIGINT, CREATE_BY TEXT, "
                + "UTC_CREATE TIMESTAMP, MODIFY_ID BIGINT, MODIFY_BY TEXT, UTC_MODIFY TIMESTAMP, REMARK TEXT, "
                + "ACCOUNT TEXT, PASSWORD TEXT, AVATAR TEXT, NICK_NAME TEXT, TEL TEXT, EMAIL TEXT, LAST_IP TEXT, "
                + "LAST_LOGIN DATE, PERM_VER BIGINT DEFAULT 0)");
        database.execute("CREATE UNIQUE INDEX UK_ACCOUNT_ACTIVE ON SYS_ACCOUNT(ACCOUNT) WHERE DELETED=0");
        return database;
    }

    private int tableCount(JdbcTemplate database) {
        return database.queryForObject("SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema='public'", Integer.class);
    }
}
