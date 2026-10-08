package org.pkaq.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 独立审查数据权限与资源权限组合及分页查询的一致性。
 *
 * @author Codex
 * @date 2026-10-07
 */
class DataPermissionCompositionReviewTest {
    private static final String QUERY_SQL = "SELECT biz.ID FROM BIZ_ORDER biz WHERE biz.DELETED = 0";

    /** 数据权限允许在资源权限关闭时独立工作。 */
    @Test
    void shouldFilterWithoutResourcePermission() {
        EvaConfig config = configuration(true);
        config.getResourcePermission().setEnable(false);
        String sql = scopedSql(config, List.of(new ThreadUser.DataScope("0007", List.of())));
        assertTrue(sql.contains("biz.CREATE_ID = 11"));
        assertTrue(sql.contains("biz.MODIFY_ID = 11"));
        assertTrue(sql.contains("biz.DELETED = 0"));
    }

    /** 关闭数据权限后，即使身份携带范围也不能注入数据条件。 */
    @Test
    void shouldLeaveSqlUntouchedWhenDisabled() {
        EvaConfig config = configuration(false);
        config.getResourcePermission().setEnable(true);
        assertEquals(QUERY_SQL, scopedSql(config, List.of(new ThreadUser.DataScope("0007", List.of()))));
        assertFalse(new MybatisPlusConfig(config).mybatisPlusInterceptor().getInterceptors().stream()
                .anyMatch(DataPermissionInterceptor.class::isInstance));
    }

    /** 已登录用户没有范围时不能获得任何业务数据。 */
    @Test
    void shouldDenyMissingScope() {
        assertTrue(scopedSql(configuration(true), List.of()).contains("1 = 0"));
    }

    /** 无法识别的范围编码不能隐式放行。 */
    @Test
    void shouldDenyUnknownScope() {
        String sql = scopedSql(configuration(true), List.of(new ThreadUser.DataScope("unknown", List.of())));
        assertTrue(sql.contains("1 = 0"));
    }

    /** 分页总数 SQL 必须继承实际数据 SQL 的同一数据范围。 */
    @Test
    void shouldCountUsingFilteredSql() {
        EvaConfig config = configuration(true);
        MybatisPlusInterceptor interceptor = new MybatisPlusConfig(config).mybatisPlusInterceptor();
        assertInstanceOf(DataPermissionInterceptor.class, interceptor.getInterceptors().get(0));
        PaginationInnerInterceptor pagination = assertInstanceOf(
                PaginationInnerInterceptor.class, interceptor.getInterceptors().get(1));
        String dataSql = scopedSql(config, List.of(new ThreadUser.DataScope("0007", List.of())));
        String countSql = pagination.autoCountSql(new Page<>(1, 20), dataSql);
        assertTrue(countSql.contains("biz.CREATE_ID = 11"));
        assertTrue(countSql.contains("biz.MODIFY_ID = 11"));
        assertTrue(countSql.contains("biz.DELETED = 0"));
    }

    private EvaConfig configuration(boolean enabled) {
        EvaConfig config = new EvaConfig();
        config.getDataPermission().setEnable(enabled);
        return config;
    }

    private String scopedSql(EvaConfig config, List<ThreadUser.DataScope> scopes) {
        Configuration mybatis = new Configuration();
        StaticSqlSource source = new StaticSqlSource(mybatis, QUERY_SQL);
        MappedStatement statement = new MappedStatement.Builder(
                mybatis, "review.BusinessMapper.selectList", source, SqlCommandType.SELECT).build();
        BoundSql boundSql = statement.getBoundSql(null);
        AtomicReference<String> sql = new AtomicReference<>();
        ThreadUser user = new ThreadUser().setUserId(11L).setDeptId(22L).setDataScopes(scopes);
        ThreadUserHelper.runWithUser(user, () -> {
            new MybatisPlusConfig(config).mybatisPlusInterceptor().getInterceptors().forEach(interceptor -> {
                try {
                    if (interceptor instanceof DataPermissionInterceptor) {
                        interceptor.beforeQuery(null, statement, null, RowBounds.DEFAULT, null, boundSql);
                    }
                } catch (SQLException exception) {
                    throw new IllegalStateException("数据权限 SQL 验证失败", exception);
                }
            });
            sql.set(boundSql.getSql());
        });
        return sql.get();
    }
}
