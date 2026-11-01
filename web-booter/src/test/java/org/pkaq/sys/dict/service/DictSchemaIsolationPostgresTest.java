package org.pkaq.sys.dict.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.fasterxml.jackson.core.JsonFactory;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.apache.ibatis.mapping.Environment;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.pkaq.config.JacksonCodeSerializer;
import org.pkaq.config.MybatisPlusDataPermissionHandler;
import org.pkaq.core.cache.util.RedisUtil;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.mybatis.tenant.TenantRoutingOrder;
import org.pkaq.core.mybatis.tenant.TenantSchemaRouter;
import org.pkaq.core.mybatis.tenant.TenantSchemaTransactionalAspect;
import org.pkaq.core.mybatis.tenant.TrustedTenantSchemaResolver;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.TenantProperties;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.SpringUtils;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.bo.DictLineBo;
import org.pkaq.sys.dict.cache.DictCacheHelper;
import org.pkaq.sys.dict.cache.DictCacheHelper.Owner;
import org.pkaq.sys.dict.cache.DictInit;
import org.pkaq.sys.dict.convert.DictConvert;
import org.pkaq.sys.dict.mapper.DictItemMapper;
import org.pkaq.sys.dict.mapper.DictMapper;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 单一隔离PG内验证C新增来源、显式列Mapper、缓存和事务链；不连接开发库。
 * @author PKAQ
 * @date 2026-11-01
 */
public class DictSchemaIsolationPostgresTest {
    private final EvaConfig config = new EvaConfig();
    private JdbcTemplate jdbc;
    private DictService service;
    private DictCacheHelper cache;
    private ConcurrentMapCacheManager caches;
    private TransactionTemplate transaction;
    private ThreadUser user;

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @EnableTransactionManagement(order = TenantRoutingOrder.TRANSACTION)
    static class Advisors {
    }

    /** 允许缓存依赖下以Java源码入口运行同一个JUnit测试方法。 */
    public static void main(String[] args) throws Exception {
        new DictSchemaIsolationPostgresTest().isolatesSourcesAndPreservesBoundaries();
        System.out.println("C_SCHEMA_ISOLATION_PASS");
    }

    @Test
    void isolatesSourcesAndPreservesBoundaries() throws Exception {
        config.setMode("saas");
        config.getTenant().setEnable(true);
        config.getTenant().setMode(TenantProperties.MODE_SCHEMA);
        config.getTenant().setCoreSchema("public");
        config.getDataPermission().setEnable(true);
        config.getResourcePermission().setEnable(false);
        config.getAuth().setPermit(new String[0]);
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder()
                .setServerConfig("listen_addresses", "127.0.0.1").start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            jdbc = new JdbcTemplate(dataSource);
            assertEquals("127.0.0.1", jdbc.queryForObject("SELECT host(inet_server_addr())", String.class));
            new TransactionTemplate(new DataSourceTransactionManager(dataSource)).executeWithoutResult(status -> {
                try {
                    initialize();
                } catch (Exception exception) {
                    throw new IllegalStateException(exception);
                }
            });
            try (AnnotationConfigApplicationContext context = context(dataSource)) {
                service = context.getBean(DictService.class);
                cache = context.getBean(DictCacheHelper.class);
                transaction = new TransactionTemplate(context.getBean(DataSourceTransactionManager.class));
                new SpringUtils().setApplicationContext(context);
                assertTrue(org.springframework.aop.support.AopUtils.isAopProxy(service));
                new DictInit(service).run();
                assertTrue(nativeCache().isEmpty());
                assertEquals("\"k\"", translate("k"));
                user = actor(101, "0000");
                ThreadUserHelper.runWithUser(user, () -> {
                    readsAndAmbiguity();
                    cacheAndConflict();
                    permissionsAndProtection();
                    writesAndTransactions();
                    missingSources();
                });
                ThreadUserHelper.runWithUser(actor(102, "0000"), () -> {
                    assertEquals("two", service.queryDict("BUS_CODE").get("k"));
                    assertEquals("core", service.queryDict("SYS_CODE").get("k"));
                });
                ThreadUserHelper.runWithUser(actor(0, "0000"), () ->
                        assertThrows(RuntimeException.class, () -> service.queryDict("SYS_CODE")));
                standalone();
                assertNull(TenantContext.schemaName());
            } finally {
                TenantContext.clear();
            }
        }
    }

    private AnnotationConfigApplicationContext context(DataSource dataSource) {
        com.baomidou.mybatisplus.core.handlers.CompositeEnumTypeHandler.setDefaultEnumTypeHandler(
                org.pkaq.core.mybatis.enums.UniversalEnumTypeHandler.class);
        MybatisConfiguration mybatis = new MybatisConfiguration();
        mybatis.setMapUnderscoreToCamelCase(true);
        mybatis.setEnvironment(new Environment("dictionary-c", new SpringManagedTransactionFactory(), dataSource));
        GlobalConfigUtils.setGlobalConfig(mybatis, new GlobalConfig().setDbConfig(new GlobalConfig.DbConfig()));
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(new MybatisPlusDataPermissionHandler(config)));
        mybatis.addInterceptor(interceptor);
        mybatis.addMapper(DictMapper.class);
        mybatis.addMapper(DictItemMapper.class);
        SqlSessionTemplate sessions = new SqlSessionTemplate(new MybatisSqlSessionFactoryBuilder().build(mybatis));
        TrustedTenantSchemaResolver resolver = new TrustedTenantSchemaResolver(jdbc, config);
        CoreSchemaExecutor core = new CoreSchemaExecutor(jdbc, config);
        DictMapper mainMapper = sessions.getMapper(DictMapper.class);
        DictItemMapper itemMapper = sessions.getMapper(DictItemMapper.class);
        caches = new ConcurrentMapCacheManager(CommonConstant.CACHE_DICTDATA);
        RedisUtil redis = mock(RedisUtil.class);
        when(redis.scanPureAll(eq(CommonConstant.CACHE_DICTDATA), anyString()))
                .thenAnswer(call -> new LinkedHashMap<>(nativeCache()));
        DictCacheHelper helper = new DictCacheHelper(caches, redis);
        DictBusinessAccess access = new DictBusinessAccess(config, jdbc, core, resolver);
        DictItemSnapshot snapshot = new DictItemSnapshot(mainMapper, jdbc, config, core, resolver);
        DictService target = new DictService(helper, itemMapper, Mappers.getMapper(DictConvert.class),
                config, snapshot, access);
        ReflectionTestUtils.setField(target, "mapper", mainMapper);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(Advisors.class);
        context.registerBean(DictMapper.class, () -> mainMapper);
        context.registerBean(DataSourceTransactionManager.class, () -> new DataSourceTransactionManager(dataSource));
        context.registerBean(TenantSchemaTransactionalAspect.class,
                () -> new TenantSchemaTransactionalAspect(new TenantSchemaRouter(dataSource, config, resolver)));
        context.registerBean(DictService.class, () -> target);
        context.registerBean(DictCacheHelper.class, () -> helper);
        context.refresh();
        return context;
    }

    private void initialize() throws Exception {
        String template = Files.readString(Path.of(System.getProperty("eva.test.resources"))
                .resolve("db/tenant-migration/V1__TENANT_SCHEMA_BASE.sql"));
        for (String schema : List.of("public", "tenant_one", "tenant_two")) {
            jdbc.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
            jdbc.execute("SET search_path TO " + schema);
            jdbc.execute(template);
            jdbc.update("INSERT INTO SYS_ACCOUNT(ID,ACCOUNT) VALUES(7,'synthetic'),(8,'ordinary')");
            jdbc.update("INSERT INTO SYS_ROLE(ID,CODE,FROZEN) VALUES(9,?,9999)",
                    "public".equals(schema) ? "ROLE_ADMIN" : "TENANT_ADMIN");
            jdbc.update("INSERT INTO SYS_ROLEUSER_REF(ROLE_ID,USER_ID) VALUES(9,7)");
            jdbc.update("INSERT INTO SYS_ROLERES_REF(ROLE_ID,RESOURCE_ID) VALUES(9,501)");
            String category = "public".equals(schema) ? "SYSTEM" : "BUSINESS";
            String code = "public".equals(schema) ? "SYS_CODE" : "BUS_CODE";
            String value = "public".equals(schema) ? "core" : "tenant_one".equals(schema) ? "one" : "two";
            jdbc.update("INSERT INTO SYS_DICT(ID,CODE,NAME,TYPE,CATEGORY,CREATE_ID) VALUES(100,?,?,?, ?,7)",
                    code, code, code, category);
            jdbc.update("INSERT INTO SYS_DICT_ITEM(ID,MAIN_ID,D_CODE,D_VALUE,CREATE_ID) VALUES(1000,100,'k',?,7)",
                    value);
        }
        jdbc.execute("SET search_path TO public");
        for (String table : List.of("SYS_ACCOUNT", "SYS_ROLE", "SYS_DICT", "SYS_DICT_ITEM")) {
            jdbc.execute("ALTER TABLE " + table + " ADD COLUMN TENANT_ID bigint DEFAULT 0");
        }
        jdbc.execute("CREATE TABLE SYS_TENANT(ID bigint, SCHEMA_NAME varchar(63), DELETED bigint, FROZEN int)");
        jdbc.update("INSERT INTO SYS_TENANT VALUES(101,'tenant_one',0,0),(102,'tenant_two',0,0)");
        jdbc.execute("CREATE TABLE SYS_MODULE(ID bigint, DELETED bigint, FROZEN int, TENANT_ID bigint)");
        jdbc.execute("CREATE TABLE SYS_MODULE_RESOURCES(ID bigint,MAIN_ID bigint,RESOURCE_URL varchar(200),"
                + "RESOURCE_TYPE varchar(20),DELETED bigint,TENANT_ID bigint)");
        jdbc.execute("CREATE TABLE SYS_TENANT_RESOURCE(TENANT_ID bigint,RESOURCE_ID bigint)");
        jdbc.update("INSERT INTO SYS_MODULE VALUES(500,0,0,0)");
        jdbc.update("INSERT INTO SYS_MODULE_RESOURCES VALUES(501,500,'/sys/dictionary','*',0,0)");
        jdbc.update("INSERT INTO SYS_TENANT_RESOURCE VALUES(101,501),(102,501)");
        assertEquals(0, jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.columns WHERE table_schema IN ('tenant_one','tenant_two')
                  AND table_name IN ('sys_dict','sys_dict_item') AND column_name='tenant_id'
                """, Integer.class));
    }

    private void readsAndAmbiguity() {
        assertEquals("one", service.queryDict("BUS_CODE").get("k"));
        assertEquals("core", service.queryDict("SYS_CODE").get("k"));
        assertNull(TenantContext.schemaName());
        assertEquals("core", service.getDict(query(100L, "SYSTEM")).getLines().getFirst().getKeyValue());
        assertEquals("one", service.getDict(query(100L, "BUSINESS")).getLines().getFirst().getKeyValue());
        assertThrows(RuntimeException.class, () -> service.getDict(query(100L, null)));
        transaction.executeWithoutResult(status -> {
            String before = jdbc.queryForObject("SELECT current_setting('search_path')", String.class);
            assertEquals("core", service.getDict(query(100L, "SYSTEM")).getLines().getFirst().getKeyValue());
            assertEquals("one", service.getDict(query(100L, "BUSINESS")).getLines().getFirst().getKeyValue());
            assertEquals(before, jdbc.queryForObject("SELECT current_setting('search_path')", String.class));
        });
        assertEquals(2, service.listWithCapability().data().size());
        assertTrue(service.listWithCapability().businessWritable());
        assertEquals("\"one\"", translate("k"));
        assertNull(TenantContext.schemaName());
        System.out.println("PASS same-ID/same-item-ID/get-category/MyBatis-session-cache/Serializer-after-route");
    }

    private void cacheAndConflict() {
        nativeCache().put("BUS_CODE", "{\"k\":\"legacy-wrong\"}");
        nativeCache().put("tenant_one::BUS_CODE", "{\"k\":\"legacy-wrong\"}");
        cache.remove(new Owner("tenant_one", "BUSINESS"), "BUS_CODE");
        assertEquals("one", service.queryDict("BUS_CODE").get("k"));
        cache.remove(new Owner("public", "SYSTEM"), "SYS_CODE");
        assertEquals(Set.of("SYS_CODE", "BUS_CODE"), service.fetchDicts().keySet());
        service.init(Map.of("forged", new LinkedHashMap<>(Map.of("k", "injected"))));
        assertFalse(nativeCache().containsKey("forged"));
        cache.cachePut(new Owner("tenant_one", "BUSINESS"), "BUS_CODE", Map.of("k", "stale"));
        service.reload(Map.of("BUS_CODE", new LinkedHashMap<>(Map.of("k", "injected"))));
        assertEquals("one", service.queryDict("BUS_CODE").get("k"));
        cache.cachePut(new Owner("tenant_two", "BUSINESS"), "BUS_CODE", Map.of("k", "two"));
        cache.removeAll(new Owner("tenant_one", "BUSINESS"));
        assertTrue(nativeCache().containsKey("v2::tenant_two::BUSINESS::BUS_CODE"));
        assertTrue(nativeCache().containsKey("BUS_CODE"));
        assertTrue(nativeCache().containsKey("v2::public::SYSTEM::SYS_CODE"));
        jdbc.update("INSERT INTO tenant_one.SYS_DICT(ID,CODE,NAME,CATEGORY) VALUES"
                + "(200,'SYS_CODE','collision','BUSINESS')");
        assertThrows(RuntimeException.class, () -> service.queryDict("SYS_CODE"));
        assertThrows(RuntimeException.class, service::fetchDicts);
        assertEquals("one", service.queryDict("BUS_CODE").get("k"));
        jdbc.update("DELETE FROM tenant_one.SYS_DICT WHERE ID=200");
        System.out.println("PASS legacy-ignored/prefix-only-eviction/partial-warm/"
                + "Map-bypass/code-conflict-before-cache");
    }

    private void permissionsAndProtection() {
        ThreadUserHelper.runWithUser(actor(101, "0007").setUserId(8), () -> {
            assertEquals("one", service.queryDict("BUS_CODE").get("k"));
            assertEquals("\"one\"", translate("k"));
            assertTrue(service.listWithCapability().data().isEmpty());
            assertFalse(service.listWithCapability().businessWritable());
            assertThrows(RuntimeException.class, () -> service.getDict(query(100L, "BUSINESS")));
            assertThrows(RuntimeException.class, () -> service.edit(edit(100L, "BUS_CODE", "bad")));
        });
        assertEquals("one", service.queryDict("BUS_CODE").get("k"));
        config.getResourcePermission().setEnable(true);
        jdbc.update("DELETE FROM public.SYS_TENANT_RESOURCE WHERE TENANT_ID=101");
        assertThrows(RuntimeException.class, () -> service.edit(edit(100L, "BUS_CODE", "bad")));
        jdbc.update("INSERT INTO public.SYS_TENANT_RESOURCE VALUES(101,501)");
        config.getResourcePermission().setEnable(false);
        jdbc.update("DELETE FROM tenant_one.SYS_ROLEUSER_REF WHERE USER_ID=7");
        assertThrows(RuntimeException.class, () -> service.edit(edit(100L, "BUS_CODE", "bad")));
        jdbc.update("INSERT INTO tenant_one.SYS_ROLEUSER_REF VALUES(9,7)");
        DictAoeBo system = edit(100L, "SYS_CODE", "bad");
        system.setCategory("SYSTEM");
        assertThrows(RuntimeException.class, () -> service.edit(system));
        assertThrows(RuntimeException.class, () -> service.edit(edit(null, "SYS_CODE", "bad")));
        jdbc.update("INSERT INTO tenant_one.SYS_DICT_ITEM(ID,MAIN_ID,D_CODE,D_VALUE,FROZEN,CREATE_ID)"
                + " VALUES(1001,100,'fixed','fixed',9999,8)");
        assertThrows(RuntimeException.class, () -> service.edit(edit(100L, "BUS_CODE", "bad")));
        assertThrows(RuntimeException.class, () -> service.delDict(100L));
        jdbc.update("INSERT INTO tenant_one.SYS_DICT(ID,CODE,NAME,CATEGORY,FROZEN,CREATE_ID)"
                + " VALUES(300,'FIXED','fixed','BUSINESS',9999,7)");
        SingleArray<Long> batch = new SingleArray<>();
        batch.setParam(Set.of(100L, 300L));
        assertThrows(RuntimeException.class, () -> service.switchFrozen(batch));
        assertEquals(0, jdbc.queryForObject("SELECT FROZEN FROM tenant_one.SYS_DICT WHERE ID=100", Integer.class));
        jdbc.update("DELETE FROM tenant_one.SYS_DICT_ITEM WHERE ID=1001");
        jdbc.update("DELETE FROM tenant_one.SYS_DICT WHERE ID=300");
        System.out.println("PASS ordinary-label-read/management-SELF/real-admin/core-ceiling/SYSTEM/9999-whole-batch");
    }

    private void writesAndTransactions() {
        Owner one = new Owner("tenant_one", "BUSINESS");
        Owner two = new Owner("tenant_two", "BUSINESS");
        cache.cachePut(one, "BUS_CODE", Map.of("k", "one"));
        cache.cachePut(two, "BUS_CODE", Map.of("k", "two"));
        transaction.executeWithoutResult(status -> {
            service.edit(edit(100L, "BUS_CODE", "rolled-back"));
            assertNotNull(cache.get(one, "BUS_CODE"));
            assertEquals("rolled-back", service.queryDict("BUS_CODE").get("k"));
            assertEquals("one", cache.get(one, "BUS_CODE").get("k"));
            assertEquals("rolled-back", service.fetchDicts().get("BUS_CODE").get("k"));
            assertEquals("one", cache.get(one, "BUS_CODE").get("k"));
            status.setRollbackOnly();
        });
        assertEquals("one", cache.get(one, "BUS_CODE").get("k"));
        assertEquals("one", jdbc.queryForObject("SELECT D_VALUE FROM tenant_one.SYS_DICT_ITEM WHERE ID=1000",
                String.class));
        cache.remove(one, "BUS_CODE");
        transaction.executeWithoutResult(status -> {
            service.edit(edit(100L, "BUS_CODE", "cold-rolled-back"));
            assertEquals("cold-rolled-back", service.queryDict("BUS_CODE").get("k"));
            assertEquals("cold-rolled-back", service.fetchDicts().get("BUS_CODE").get("k"));
            assertNull(cache.get(one, "BUS_CODE"));
            status.setRollbackOnly();
        });
        assertNull(cache.get(one, "BUS_CODE"));
        assertEquals("one", service.queryDict("BUS_CODE").get("k"));
        config.getResourcePermission().setEnable(true);
        transaction.executeWithoutResult(status -> {
            service.edit(edit(100L, "BUS_CODE", "updated"));
            TenantContext.bind(102L, "tenant_two");
        });
        assertEquals(102L, TenantContext.tenantId());
        assertEquals("tenant_two", TenantContext.schemaName());
        TenantContext.clear();
        assertNull(cache.get(one, "BUS_CODE"));
        assertEquals("two", cache.get(two, "BUS_CODE").get("k"));
        assertEquals("updated", service.queryDict("BUS_CODE").get("k"));
        assertEquals(7L, jdbc.queryForObject("SELECT MODIFY_ID FROM tenant_one.SYS_DICT_ITEM WHERE ID=1000",
                Long.class));
        assertEquals(1, jdbc.queryForObject("SELECT REVISION FROM tenant_one.SYS_DICT_ITEM WHERE ID=1000",
                Integer.class));
        DictAoeBo stale = edit(100L, "BUS_CODE", "stale");
        stale.setRevision(0);
        assertThrows(RuntimeException.class, () -> service.edit(stale));
        assertEquals("updated", service.queryDict("BUS_CODE").get("k"));
        assertNotNull(cache.get(one, "BUS_CODE"));
        DictAoeBo renamed = edit(100L, "RENAMED", "renamed");
        service.edit(renamed);
        assertNull(cache.get(one, "BUS_CODE"));
        assertNull(cache.get(one, "RENAMED"));
        assertEquals("renamed", service.queryDict("RENAMED").get("k"));
        DictAoeBo inserted = edit(null, "NEW", "new");
        inserted.getLines().getFirst().setId(null);
        service.edit(inserted);
        assertEquals("new", service.queryDict("NEW").get("k"));
        service.delDict(100L);
        assertTrue(jdbc.queryForObject("SELECT DELETED>0 FROM tenant_one.SYS_DICT WHERE ID=100", Boolean.class));
        assertTrue(jdbc.queryForObject("SELECT DELETED>0 FROM tenant_one.SYS_DICT_ITEM WHERE ID=1000", Boolean.class));
        assertEquals(0L, jdbc.queryForObject("SELECT DELETED FROM tenant_two.SYS_DICT_ITEM WHERE ID=1000", Long.class));
        assertEquals("core", service.queryDict("SYS_CODE").get("k"));
        config.getResourcePermission().setEnable(false);
        System.out.println("PASS private-no-tenant-column/insert-update-delete/audit-revision/"
                + "rollback/owner-old-new-eviction");
    }

    private void missingSources() {
        jdbc.execute("ALTER TABLE tenant_one.SYS_DICT_ITEM RENAME TO SYS_DICT_ITEM_HIDDEN");
        assertThrows(RuntimeException.class, () -> service.queryDict("SYS_CODE"));
        jdbc.execute("ALTER TABLE tenant_one.SYS_DICT_ITEM_HIDDEN RENAME TO SYS_DICT_ITEM");
        jdbc.execute("ALTER TABLE tenant_one.SYS_DICT DROP CONSTRAINT CK_SYS_DICT_CATEGORY");
        assertThrows(RuntimeException.class, () -> service.queryDict("SYS_CODE"));
        jdbc.execute("ALTER TABLE tenant_one.SYS_DICT ADD CONSTRAINT CK_SYS_DICT_CATEGORY"
                + " CHECK(CATEGORY IS NULL OR CATEGORY IN ('SYSTEM','BUSINESS'))");
        jdbc.execute("ALTER TABLE tenant_one.SYS_DICT_ITEM RENAME COLUMN D_VALUE TO D_VALUE_HIDDEN");
        assertThrows(RuntimeException.class, () -> service.queryDict("SYS_CODE"));
        jdbc.execute("ALTER TABLE tenant_one.SYS_DICT_ITEM RENAME COLUMN D_VALUE_HIDDEN TO D_VALUE");
        TenantContext.bind(102L, "tenant_two");
        try {
            assertEquals("new", service.queryDict("NEW").get("k"));
            assertEquals(102L, TenantContext.tenantId());
            assertEquals("tenant_two", TenantContext.schemaName());
        } finally {
            TenantContext.clear();
        }
        System.out.println("PASS missing-private-table/column/category-constraint/actor-source-and-ambient-restore");
    }

    private void standalone() {
        config.setMode("standalone");
        config.getTenant().setEnable(false);
        config.getTenant().setStandaloneSchema("public");
        ThreadUserHelper.runWithUser(actor(0, "0000"), () -> {
            assertEquals("core", service.queryDict("SYS_CODE").get("k"));
            assertTrue(service.listWithCapability().businessWritable());
            assertTrue(service.getDict(query(100L, "SYSTEM")).isReadOnly());
        });
        assertTrue(nativeCache().containsKey("v2::public::SYSTEM::SYS_CODE"));
        System.out.println("PASS standalone-v2-and-existing-SYSTEM-readonly/platform-zero-rejected-in-schema-mode");
    }

    @SuppressWarnings("unchecked")
    private Map<Object, Object> nativeCache() {
        return (Map<Object, Object>) caches.getCache(CommonConstant.CACHE_DICTDATA).getNativeCache();
    }

    private static ThreadUser actor(long tenant, String scope) {
        return new ThreadUser().setUserId(7L).setName("synthetic").setTenantId(tenant)
                .setDataScopes(List.of(new ThreadUser.DataScope(scope, List.of())));
    }

    private static DictAoeBo query(Long id, String category) {
        DictAoeBo bo = new DictAoeBo();
        bo.setId(id);
        bo.setCategory(category);
        return bo;
    }

    private static DictAoeBo edit(Long id, String code, String value) {
        DictAoeBo bo = query(id, "BUSINESS");
        bo.setCode(code);
        bo.setName(code);
        DictLineBo line = new DictLineBo();
        line.setId(1000L);
        line.setKeyName("k");
        line.setKeyValue(value);
        bo.setLines(List.of(line));
        return bo;
    }

    private static String translate(String value) {
        try {
            StringWriter output = new StringWriter();
            try (var generator = new JsonFactory().createGenerator(output)) {
                new JacksonCodeSerializer("BUS_CODE").serialize(value, generator, null);
            }
            return output.toString();
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
