package org.pkaq.sys.role.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.mapstruct.factory.Mappers;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.pkaq.config.MybatisPlusConfig;
import org.pkaq.core.account.IAccountCreation;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.mybatis.tenant.TrustedTenantSchemaResolver;
import org.pkaq.core.mybatis.tenant.TenantSchemaRouter;
import org.pkaq.core.mybatis.tenant.TenantSchemaTransactionalAspect;
import org.pkaq.core.mybatis.tenant.TenantRoutingOrder;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.upload.provider.FileProvider;
import org.pkaq.sys.module.convert.ModuleConvert;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.module.service.ModuleService;
import org.pkaq.sys.organization.mapper.OrganizationMapper;
import org.pkaq.sys.post.mapper.PostMapper;
import org.pkaq.sys.post.mapper.PostUserMapper;
import org.pkaq.sys.post.service.UserPostRefSerivce;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.role.mapper.RoleMapper;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.user.convert.UserConvert;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.AccountProfileMapper;
import org.pkaq.sys.user.mapper.UserMapper;
import org.pkaq.sys.user.service.UserService;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 真实用户详情、Mapper/XML/数据插件与Spring事务，JDBC只包含合成数据。
 * @author PKAQ
 * @date 2026-10-24
 */
final class AssignedRoleDetailJdbcFixture {
    final EvaConfig config = new EvaConfig();
    final DataSource dataSource = mock(DataSource.class);
    final Connection connection;
    final UserService service;
    final UserRoleRefSerivce roleReads;
    final AccountProfileMapper profiles = mock(AccountProfileMapper.class);
    final IAccountCreation accountCreation = mock(IAccountCreation.class);
    final List<Long> assigned = new ArrayList<>();
    final List<Long> posts = new ArrayList<>();
    final Map<String, List<Long>> postsBySchema = new HashMap<>();
    final Map<String, List<Map<String, Object>>> roles = new HashMap<>();
    final Map<String, String> namespaces = new HashMap<>();
    final List<String> sql = new ArrayList<>();
    final List<Map<Integer, Object>> parameters = new ArrayList<>();
    final List<String> schemaTransitions = new ArrayList<>();
    final List<String> queryOrigins = new ArrayList<>();
    int controlPlaneLookups;
    int shadowLookups;
    boolean tenantControlShadow;
    String controlNamespace = "eva_core";
    int referenceReads;
    int roleQueries;
    int accountQueries;
    int writes;
    String currentSchema = "eva";
    String searchPath = "eva";
    String failingTable;
    boolean catalogFailure;
    boolean accountPresent = true;
    long accountCreator = 7L;
    int accountFrozen;
    long target = 2113894345662021633L;

    AssignedRoleDetailJdbcFixture(boolean dataEnabled) throws Exception {
        this(dataEnabled, false);
    }

    AssignedRoleDetailJdbcFixture(boolean dataEnabled, boolean routedEntry) throws Exception {
        config.getDataPermission().setEnable(dataEnabled);
        for (String table : List.of("sys_account", "sys_account_profile", "sys_roleuser_ref", "sys_role",
                "sys_organization", "sys_postuser_ref")) {
            namespaces.put(table, "eva");
        }
        AtomicBoolean autoCommit = new AtomicBoolean(true);
        connection = mock(Connection.class, invocation -> switch (invocation.getMethod().getName()) {
            case "getAutoCommit" -> autoCommit.get();
            case "setAutoCommit" -> {
                autoCommit.set(invocation.getArgument(0));
                yield null;
            }
            case "prepareStatement" -> statement(invocation.getArgument(0));
            case "createStatement" -> plainStatement();
            default -> RETURNS_DEFAULTS.answer(invocation);
        });
        when(dataSource.getConnection()).thenReturn(connection);
        MybatisConfiguration mapperConfig = new MybatisConfiguration();
        mapperConfig.setEnvironment(new Environment("synthetic", new SpringManagedTransactionFactory(), dataSource));
        mapperConfig.getTypeAliasRegistry().registerAlias("user", UserEntity.class);
        mapperConfig.getTypeAliasRegistry().registerAlias("role", RoleEntity.class);
        mapperConfig.getTypeAliasRegistry().registerAlias("module", ModuleEntity.class);
        mapperConfig.addInterceptor(new MybatisPlusConfig(config).mybatisPlusInterceptor());
        for (String resource : List.of("mapper/common.xml", "mapper/User.xml", "mapper/UserRoleRef.xml")) {
            try (InputStream stream = Resources.getResourceAsStream(resource)) {
                new XMLMapperBuilder(stream, mapperConfig, resource, mapperConfig.getSqlFragments()).parse();
            }
        }
        mapperConfig.addMapper(RoleMapper.class);
        mapperConfig.addMapper(PostUserMapper.class);
        SqlSessionTemplate session = new SqlSessionTemplate(new MybatisSqlSessionFactoryBuilder().build(mapperConfig));
        UserMapper users = session.getMapper(UserMapper.class);
        RoleUserMapper refs = session.getMapper(RoleUserMapper.class);
        PostUserMapper postRefs = session.getMapper(PostUserMapper.class);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.setExceptionTranslator((task, query, failure) ->
                new org.springframework.jdbc.UncategorizedSQLException(task, query, failure));
        CoreSchemaExecutor core = new CoreSchemaExecutor(jdbc, config);
        roleReads = new UserRoleRefSerivce(refs, session.getMapper(RoleMapper.class), users, jdbc, config,
                new TrustedTenantSchemaResolver(jdbc, config), core);
        UserPostRefSerivce postService = new UserPostRefSerivce(postRefs, mock(PostMapper.class), users);
        AspectJProxyFactory postProxy = new AspectJProxyFactory(postService);
        postProxy.setProxyTargetClass(true);
        postProxy.addAspect(new TenantSchemaTransactionalAspect(new TenantSchemaRouter(dataSource, config,
                new TrustedTenantSchemaResolver(jdbc, config))));
        UserService userService = new UserService(roleReads, postProxy.getProxy(), mock(FileProvider.class),
                refs, postRefs,
                mock(OrganizationMapper.class), mock(ModuleService.class), mock(ModuleConvert.class),
                Mappers.getMapper(UserConvert.class), config, mock(ApplicationEventPublisher.class), core,
                mock(RoleResourceMapper.class), accountCreation, profiles);
        ReflectionTestUtils.setField(userService, "mapper", users);
        TransactionInterceptor transactions = new TransactionInterceptor();
        transactions.setTransactionManager(new DataSourceTransactionManager(dataSource));
        transactions.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        AspectJProxyFactory proxy = new AspectJProxyFactory(userService);
        proxy.setProxyTargetClass(true);
        DefaultPointcutAdvisor transactionAdvisor = new DefaultPointcutAdvisor(transactions);
        transactionAdvisor.setOrder(TenantRoutingOrder.TRANSACTION);
        proxy.addAdvisor(transactionAdvisor);
        if (routedEntry) {
            proxy.addAspect(new TenantSchemaTransactionalAspect(new TenantSchemaRouter(dataSource, config,
                    new TrustedTenantSchemaResolver(jdbc, config))));
        }
        service = proxy.getProxy();
    }

    void role(String schema, long id, String name, String code, Integer frozen, long deleted, long creator) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("name", name);
        row.put("code", code);
        row.put("frozen", frozen);
        row.put("deleted", deleted);
        row.put("create_id", creator);
        row.put("modify_id", creator);
        roles.computeIfAbsent(schema, key -> new ArrayList<>()).add(row);
    }

    void source(String schema) {
        currentSchema = schema;
        searchPath = schema + ",eva_core";
        namespaces.replaceAll((table, previous) -> schema);
    }

    private PreparedStatement statement(String query) {
        assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
        ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        assertSame(connection, Objects.requireNonNull(holder).getConnection());
        assertTrue(connectionIsReadOnly());
        Map<Integer, Object> bound = new HashMap<>();
        AtomicReference<ResultSet> result = new AtomicReference<>();
        return mock(PreparedStatement.class, invocation -> {
            String method = invocation.getMethod().getName();
            if (method.startsWith("set") && invocation.getArguments().length >= 2
                    && invocation.getArgument(0) instanceof Integer index) {
                bound.put(index, "setNull".equals(method) ? null : invocation.getArgument(1));
                return null;
            }
            if ("execute".equals(method) || "executeQuery".equals(method)) {
                result.set(execute(query, bound));
                return "execute".equals(method) ? true : result.get();
            }
            return switch (method) {
                case "getResultSet" -> result.get();
                case "getUpdateCount" -> -1;
                case "getConnection" -> connection;
                default -> RETURNS_DEFAULTS.answer(invocation);
            };
        });
    }

    private boolean connectionIsReadOnly() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }

    private Statement plainStatement() {
        assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
        ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        assertSame(connection, Objects.requireNonNull(holder).getConnection());
        assertTrue(connectionIsReadOnly());
        return mock(Statement.class, invocation -> {
            if ("executeQuery".equals(invocation.getMethod().getName())) {
                return execute(invocation.getArgument(0), Map.of());
            }
            if ("getConnection".equals(invocation.getMethod().getName())) {
                return connection;
            }
            return RETURNS_DEFAULTS.answer(invocation);
        });
    }

    private ResultSet execute(String query, Map<Integer, Object> bound) throws Exception {
        sql.add(query);
        parameters.add(new HashMap<>(bound));
        if (!query.stripLeading().toLowerCase().startsWith("select")) {
            writes++;
            throw new AssertionError("只读测试禁止写SQL");
        }
        if (query.contains("control_schema_name")) {
            assertEquals("sys_tenant", bound.get(1));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("control_schema_name", currentSchema);
            row.put("control_relation_schema", controlNamespace);
            return resultSet(List.of(row), List.of("control_schema_name", "control_relation_schema"));
        }
        if (query.contains("pg_catalog.current_schema()")) {
            if (catalogFailure) {
                throw new SQLException("合成来源核验失败");
            }
            assertEquals(6, bound.size());
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Object table : bound.values()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("current_schema_name", currentSchema);
                row.put("relation_name", table);
                row.put("relation_schema_name", namespaces.get(table));
                rows.add(row);
            }
            return resultSet(rows, List.of("current_schema_name", "relation_name", "relation_schema_name"));
        }
        if (query.contains("current_setting('search_path')")) {
            return resultSet(List.of(Map.of("value", searchPath)), List.of("value"));
        }
        if (query.contains("set_config('search_path'")) {
            searchPath = (String) bound.get(1);
            currentSchema = searchPath.split(",")[0];
            schemaTransitions.add(searchPath);
            return resultSet(List.of(Map.of("value", searchPath)), List.of("value"));
        }
        if (query.contains("FROM SYS_TENANT")) {
            String mapped;
            if ("eva_core".equals(currentSchema)) {
                controlPlaneLookups++;
                mapped = Map.of(22L, "tenant_a", 23L, "tenant_b").get(bound.get(1));
            } else {
                shadowLookups++;
                assertTrue(tenantControlShadow, "映射必须从权威core读取");
                mapped = "tenant_b";
            }
            return resultSet(mapped == null ? List.of() : List.of(Map.of("schema_name", mapped)),
                    List.of("schema_name"));
        }
        var select = ((Select) CCJSqlParserUtil.parse(query)).getPlainSelect();
        String table = ((Table) select.getFromItem()).getName().toLowerCase();
        queryOrigins.add(table + "@" + currentSchema);
        if (table.equals(failingTable)) {
            throw new SQLException("合成Mapper异常");
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns;
        switch (table) {
            case "sys_account" -> {
                accountQueries++;
                if (accountPresent) {
                    rows.add(new HashMap<>(Map.of("id", target, "dept_id", 42L, "deleted", 0L,
                            "frozen", accountFrozen, "create_id", accountCreator, "modify_id", accountCreator)));
                }
                columns = List.of("id", "dept_id", "deleted", "frozen", "create_id", "modify_id");
            }
            case "sys_roleuser_ref" -> {
                referenceReads++;
                for (Long id : assigned) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("user_id", target);
                    row.put("role_id", id);
                    rows.add(row);
                }
                rows.add(Map.of("user_id", 88L, "role_id", 99L));
                columns = List.of("role_id");
            }
            case "sys_role" -> {
                roleQueries++;
                rows.addAll(roles.getOrDefault(currentSchema, List.of()));
                columns = List.of("id", "name", "code", "frozen");
                System.out.println("ASSIGNED_SYNTHETIC_BOUND_SQL=" + query + "; PARAMETERS=" + bound);
            }
            case "sys_postuser_ref" -> {
                for (Long id : postsBySchema.getOrDefault(currentSchema, posts)) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("user_id", target);
                    row.put("post_id", id);
                    rows.add(row);
                }
                columns = List.of("post_id");
            }
            default -> throw new AssertionError("不允许读取其它合成表: " + table);
        }
        Expression where = select.getWhere();
        List<Map<String, Object>> matching = rows.stream().filter(row -> test(where, row, bound)).toList();
        return resultSet(matching, columns);
    }
    private boolean test(Expression expression, Map<String, Object> row, Map<Integer, Object> bound) {
        if (expression instanceof AndExpression and) {
            return test(and.getLeftExpression(), row, bound) && test(and.getRightExpression(), row, bound);
        }
        if (expression instanceof OrExpression or) {
            return test(or.getLeftExpression(), row, bound) || test(or.getRightExpression(), row, bound);
        }
        if (expression instanceof EqualsTo equal) {
            Object left = value(equal.getLeftExpression(), row, bound);
            Object right = value(equal.getRightExpression(), row, bound);
            return left != null && right != null && (left instanceof Number a && right instanceof Number b
                    ? a.longValue() == b.longValue() : left.equals(right));
        }
        if (expression instanceof IsNullExpression isNull) {
            return (value(isNull.getLeftExpression(), row, bound) == null) != isNull.isNot();
        }
        if (expression instanceof InExpression in && in.getRightExpression() instanceof ExpressionList<?> values) {
            Object left = value(in.getLeftExpression(), row, bound);
            return values.stream().anyMatch(item -> Objects.equals(left, value((Expression) item, row, bound)));
        }
        if (expression instanceof ExpressionList<?> values && values.size() == 1) {
            return test((Expression) values.getFirst(), row, bound);
        }
        throw new AssertionError("测试SQL包含未支持的谓词: " + expression.getClass().getSimpleName());
    }

    private Object value(Expression expression, Map<String, Object> row, Map<Integer, Object> bound) {
        if (expression instanceof Function function && "COALESCE".equalsIgnoreCase(function.getName())) {
            for (Object parameter : function.getParameters()) {
                Object resolved = value((Expression) parameter, row, bound);
                if (resolved != null) {
                    return resolved;
                }
            }
            return null;
        }
        if (expression instanceof Column column) {
            return row.get(column.getColumnName().toLowerCase());
        }
        if (expression instanceof LongValue number) {
            return number.getValue();
        }
        if (expression instanceof JdbcParameter parameter) {
            assertTrue(bound.containsKey(parameter.getIndex()));
            return bound.get(parameter.getIndex());
        }
        throw new AssertionError("测试SQL包含未支持的参数: " + expression.getClass().getSimpleName());
    }

    private ResultSet resultSet(List<Map<String, Object>> rows, List<String> columns) throws Exception {
        AtomicInteger cursor = new AtomicInteger(-1);
        AtomicBoolean wasNull = new AtomicBoolean();
        ResultSetMetaData metadata = mock(ResultSetMetaData.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("getColumnCount".equals(method)) {
                return columns.size();
            }
            if ("getColumnLabel".equals(method) || "getColumnName".equals(method)) {
                return columns.get((int) invocation.getArgument(0) - 1);
            }
            if ("getColumnType".equals(method)) {
                return columns.size() == 1 ? Types.BIGINT : Types.VARCHAR;
            }
            return RETURNS_DEFAULTS.answer(invocation);
        });
        return mock(ResultSet.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("next".equals(method)) {
                return cursor.incrementAndGet() < rows.size();
            }
            if ("getMetaData".equals(method)) {
                return metadata;
            }
            if ("wasNull".equals(method)) {
                return wasNull.get();
            }
            if (List.of("getObject", "getLong", "getString").contains(method)) {
                Object key = invocation.getArgument(0);
                String column = key instanceof Integer index ? columns.get(index - 1) : key.toString().toLowerCase();
                Object result = rows.get(cursor.get()).get(column);
                wasNull.set(result == null);
                return switch (method) {
                    case "getLong" -> result == null ? 0L : ((Number) result).longValue();
                    case "getString" -> result == null ? null : result.toString();
                    default -> result;
                };
            }
            return RETURNS_DEFAULTS.answer(invocation);
        });
    }
}
