package org.pkaq.config;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.core.properties.DataPermission;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 数据权限 SQL 生成回归测试。
 *
 * @author PKAQ
 */
class MybatisPlusDataPermissionHandlerTest {
    private MybatisPlusDataPermissionHandler handler;

    @BeforeEach
    void setUp() {
        EvaConfig evaConfig = new EvaConfig();
        DataPermission dataPermission = new DataPermission();
        dataPermission.setEnable(true);
        evaConfig.setDataPermission(dataPermission);
        this.handler = new MybatisPlusDataPermissionHandler(evaConfig);
    }

    /** 未登录的后台任务不得被附加用户数据权限。 */
    @Test
    void shouldSkipWhenUserContextIsAbsent() {
        assertNull(this.handler.getSqlSegment(table(), null, "example.Mapper.selectList"));
    }

    /** 登录用户没有有效角色范围时必须拒绝所有数据。 */
    @Test
    void shouldDenyWhenScopeIsEmpty() {
        assertEquals("1 = 0", expressionOf(List.of()).toString());
    }

    /** 全部数据范围不得缩小结果集。 */
    @Test
    void shouldAllowAllScope() {
        assertEquals("1 = 1", expressionOf(List.of(scope("0000"))).toString());
    }

    /** 本人范围必须同时检查创建人与修改人的用户 ID。 */
    @Test
    void shouldBuildCreatorScopeWithAlias() {
        String sql = expressionOf(List.of(scope("0007"))).toString();
        assertTrue(sql.contains("biz.CREATE_ID = 11"));
        assertTrue(sql.contains("biz.MODIFY_ID = 11"));
    }

    /** 部门及下级范围必须使用带路径分隔符的组织树匹配。 */
    @Test
    void shouldBuildDepartmentTreeScope() {
        String sql = expressionOf(List.of(scope("0002"))).toString();
        assertTrue(sql.contains("dp_user.DEPT_ID"));
        assertTrue(sql.contains("dp_org.ID = 22"));
        assertTrue(sql.contains("%/22/%"));
    }

    /** 指定部门范围只允许结构化的正整数 ID。 */
    @Test
    void shouldBuildSpecifiedDepartmentScope() {
        ThreadUser.DataScope scope = new ThreadUser.DataScope("0003", List.of(31L, 32L));
        String sql = expressionOf(List.of(scope)).toString();
        assertTrue(sql.contains("dp_user.DEPT_ID IN (31, 32)"));
    }

    /** Ignore 注解必须跳过整个 Mapper 的数据权限。 */
    @Test
    void shouldHonorIgnoreAnnotation() {
        AtomicReference<Expression> result = new AtomicReference<>();
        ThreadUserHelper.runWithUser(user(List.of(scope("0007"))), () -> result.set(this.handler.getSqlSegment(
                table(), null, IgnoredMapper.class.getName() + ".selectList")));
        assertNull(result.get());
    }

    private Expression expressionOf(List<ThreadUser.DataScope> scopes) {
        AtomicReference<Expression> result = new AtomicReference<>();
        ThreadUserHelper.runWithUser(user(scopes), () -> result.set(this.handler.getSqlSegment(
                table(), null, "example.Mapper.selectList")));
        return result.get();
    }

    private ThreadUser user(List<ThreadUser.DataScope> scopes) {
        return new ThreadUser().setUserId(11L).setDeptId(22L).setDataScopes(scopes);
    }

    private ThreadUser.DataScope scope(String code) {
        return new ThreadUser.DataScope(code, List.of());
    }

    private Table table() {
        Table table = new Table("BIZ_ORDER");
        table.setAlias(new Alias("biz"));
        return table;
    }

    @Ignore
    private interface IgnoredMapper {
    }
}
