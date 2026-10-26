package org.pkaq.sys.role.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mapstruct.factory.Mappers;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.pkaq.config.MybatisPlusConfig;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.sys.module.convert.ModuleConvert;
import org.pkaq.sys.module.mapper.ModuleMapper;
import org.pkaq.sys.organization.mapper.OrganizationMapper;
import org.pkaq.sys.role.convert.RoleConvert;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.role.mapper.RoleMapper;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.tenant.mapper.TenantResourceMapper;
import org.pkaq.sys.user.convert.UserConvert;
import org.pkaq.sys.user.mapper.UserMapper;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
 * 真实Mapper、参数绑定、数据插件与事务，仅JDBC为内存替身；按实际SQL AST筛选合成组织行。
 * @author PKAQ
 * @date 2026-10-24
 */
final class CustomScopeJdbcFixture {
    final EvaConfig config = new EvaConfig();
    final RoleMapper roleMapper = mock(RoleMapper.class);
    final RoleUserMapper roleUserMapper = mock(RoleUserMapper.class);
    final RoleResourceMapper roleResourceMapper = mock(RoleResourceMapper.class);
    final UserMapper userMapper = mock(UserMapper.class);
    final List<Map<String, Object>> organizations = new ArrayList<>();
    final List<String> organizationSql = new ArrayList<>();
    final List<Map<Integer, Object>> parameterBindings = new ArrayList<>();
    final AtomicInteger schemaQueries = new AtomicInteger();
    final DataSource dataSource = mock(DataSource.class);
    final Connection connection;
    final RoleService service;
    final RoleService unproxied;
    String currentSchema = "eva";
    String tableSchema = "eva";
    boolean schemaFailure;
    boolean organizationFailure;

    CustomScopeJdbcFixture(boolean dataEnabled) throws Exception {
        config.getDataPermission().setEnable(dataEnabled);
        AtomicBoolean autoCommit = new AtomicBoolean(true);
        connection = mock(Connection.class, invocation -> switch (invocation.getMethod().getName()) {
            case "getAutoCommit" -> autoCommit.get();
            case "setAutoCommit" -> {
                autoCommit.set(invocation.getArgument(0));
                yield null;
            }
            case "prepareStatement" -> statement(invocation.getArgument(0));
            default -> RETURNS_DEFAULTS.answer(invocation);
        });
        when(dataSource.getConnection()).thenReturn(connection);
        MybatisConfiguration mapperConfig = new MybatisConfiguration();
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(mapperConfig, ""), RoleUserEntity.class);
        mapperConfig.setEnvironment(new Environment("isolated", new SpringManagedTransactionFactory(), dataSource));
        mapperConfig.addInterceptor(new MybatisPlusConfig(config).mybatisPlusInterceptor());
        mapperConfig.addMapper(OrganizationMapper.class);
        OrganizationMapper organizationMapper = new SqlSessionTemplate(
                new MybatisSqlSessionFactoryBuilder().build(mapperConfig)).getMapper(OrganizationMapper.class);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.setExceptionTranslator((task, query, failure) ->
                new org.springframework.jdbc.UncategorizedSQLException(task, query, failure));
        unproxied = new RoleService(organizationMapper, roleResourceMapper, roleUserMapper,
                mock(ModuleMapper.class), userMapper, mock(TenantResourceMapper.class), mock(UserConvert.class),
                Mappers.getMapper(RoleConvert.class), mock(ModuleConvert.class), mock(ApplicationEventPublisher.class),
                new CoreSchemaExecutor(jdbc, config), config, jdbc);
        ReflectionTestUtils.setField(unproxied, "mapper", roleMapper);
        TransactionInterceptor transactions = new TransactionInterceptor();
        transactions.setTransactionManager(new DataSourceTransactionManager(dataSource));
        transactions.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        AspectJProxyFactory proxy = new AspectJProxyFactory(unproxied);
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(transactions);
        service = proxy.getProxy();
        row(12L, null, 0L, 7L);
        row(13L, 0L, 0L, 7L);
        row(14L, 22L, 0L, 7L);
        row(15L, null, 1L, 7L);
        row(16L, null, 0L, 88L);
        row(99L, null, 0L, 7L);
    }

    private void row(long id, Long tenant, long deleted, long creator) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("tenant_id", tenant);
        row.put("deleted", deleted);
        row.put("create_id", creator);
        row.put("modify_id", creator);
        organizations.add(row);
    }

    private PreparedStatement statement(String query) {
        assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
        ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        assertSame(connection, Objects.requireNonNull(holder).getConnection());
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

    private ResultSet execute(String query, Map<Integer, Object> bound) throws Exception {
        if (query.contains("pg_catalog.current_schema()")) {
            schemaQueries.incrementAndGet();
            assertEquals("sys_organization", bound.get(1));
            if (schemaFailure) {
                throw new SQLException("合成schema核验异常");
            }
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("current_schema_name", currentSchema);
            metadata.put("organization_schema_name", tableSchema);
            return resultSet(List.of(metadata), List.of("current_schema_name", "organization_schema_name"));
        }
        assertTrue(query.toLowerCase().contains("sys_organization"));
        organizationSql.add(query);
        parameterBindings.add(new HashMap<>(bound));
        if (organizationFailure) {
            throw new SQLException("合成tenant schema缺tenant_id列");
        }
        Expression where = ((Select) CCJSqlParserUtil.parse(query)).getPlainSelect().getWhere();
        List<Map<String, Object>> matching = new ArrayList<>();
        for (Map<String, Object> row : organizations) {
            if (test(where, row, bound)) {
                matching.add(Map.of("id", row.get("id")));
            }
        }
        return resultSet(matching, List.of("id"));
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
