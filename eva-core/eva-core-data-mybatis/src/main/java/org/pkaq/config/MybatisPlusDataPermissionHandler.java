package org.pkaq.config;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.core.mybatis.enums.DataPermissionEnumm;
import org.pkaq.core.properties.DataPermission;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * MyBatis-Plus 数据权限处理器。
 *
 * @author PKAQ
 */
@Slf4j
@RequiredArgsConstructor
public class MybatisPlusDataPermissionHandler implements MultiDataPermissionHandler {
    private static final String DENY_ALL_SQL = "1 = 0";
    private static final Set<String> INTERNAL_TABLES = Set.of(
            "sys_roleuser_ref", "sys_roleres_ref", "sys_postuser_ref",
            "sys_tenant_resource", "sys_tenant_package_resource", "sys_tenant_role");

    private final EvaConfig evaConfig;
    private final ConcurrentMap<String, Boolean> ignoredStatementCache = new ConcurrentHashMap<>();

    /**
     * 为当前表生成数据权限条件。
     *
     * @param table 数据表
     * @param where 原查询条件
     * @param mappedStatementId Mapper 方法标识
     * @return 需要追加的数据权限条件；无需限制时返回 null
     */
    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        DataPermission config = this.evaConfig.getDataPermission();
        if (!config.isEnable() || shouldIgnore(table, mappedStatementId, config)) {
            return null;
        }
        ThreadUser currentUser = ThreadUserHelper.getCurrentUserOrNull();
        if (currentUser == null) {
            return null;
        }

        try {
            return CCJSqlParserUtil.parseCondExpression(buildPermissionSql(table, currentUser));
        } catch (Exception exception) {
            log.error("数据权限 SQL 生成失败，statementId={}, table={}", mappedStatementId, table, exception);
            try {
                return CCJSqlParserUtil.parseCondExpression(DENY_ALL_SQL, false);
            } catch (Exception denyException) {
                throw new IllegalStateException("无法生成拒绝访问的数据权限条件", denyException);
            }
        }
    }

    private String buildPermissionSql(Table table, ThreadUser currentUser) {
        List<ThreadUser.DataScope> scopes = currentUser.getDataScopes();
        if (scopes == null || scopes.isEmpty()) {
            return DENY_ALL_SQL;
        }
        if (scopes.stream().anyMatch(scope -> DataPermissionEnumm.ALL.getCode().equals(scope.getCode()))) {
            return "1 = 1";
        }

        String qualifier = table.getAlias() == null ? table.getName() : table.getAlias().getName();
        String createId = qualifier + ".CREATE_ID";
        String modifyId = qualifier + ".MODIFY_ID";
        long userId = currentUser.getUserId();
        long deptId = currentUser.getDeptId();
        Set<String> conditions = new HashSet<>();
        for (ThreadUser.DataScope scope : scopes) {
            DataPermissionEnumm permission = DataPermissionEnumm.getByCode(scope.getCode());
            if (permission == null) {
                continue;
            }
            switch (permission) {
                case CREATOR_LIMIT -> conditions.add(ownerCondition(createId, modifyId, userId));
                case DEPT_ONLY_LIMIT -> addDepartmentCondition(conditions, createId, modifyId, deptId);
                case DEPT_AND_CHILDREN_LIMIT -> addDepartmentTreeCondition(
                        conditions, createId, modifyId, deptId);
                case DEPT_LIMIT -> addSpecifiedDepartmentCondition(
                        conditions, createId, modifyId, scope.getOrgIds());
                default -> {
                    // 未实现的权限类型不得隐式放行。
                }
            }
        }
        return conditions.isEmpty() ? DENY_ALL_SQL : "(" + String.join(" OR ", conditions) + ")";
    }

    private void addDepartmentCondition(Set<String> conditions, String createId, String modifyId, long deptId) {
        if (deptId > 0L) {
            conditions.add(departmentCondition(createId, modifyId, "dp_user.DEPT_ID = " + deptId));
        }
    }

    private void addDepartmentTreeCondition(Set<String> conditions, String createId, String modifyId, long deptId) {
        if (deptId <= 0L) {
            return;
        }
        String predicate = "dp_user.DEPT_ID IN (SELECT dp_org.ID FROM SYS_ORGANIZATION dp_org "
                + "WHERE dp_org.DELETED = 0 AND (dp_org.ID = " + deptId
                + " OR dp_org.PATH LIKE '%/" + deptId + "/%'))";
        conditions.add(departmentCondition(createId, modifyId, predicate));
    }

    private void addSpecifiedDepartmentCondition(Set<String> conditions,
                                                  String createId,
                                                  String modifyId,
                                                  Collection<Long> ids) {
        String orgIds = validIds(ids);
        if (!orgIds.isEmpty()) {
            conditions.add(departmentCondition(createId, modifyId, "dp_user.DEPT_ID IN (" + orgIds + ")"));
        }
    }

    private String ownerCondition(String createId, String modifyId, long userId) {
        return "(" + createId + " = " + userId + " OR " + modifyId + " = " + userId + ")";
    }

    private String departmentCondition(String createId, String modifyId, String departmentPredicate) {
        return "EXISTS (SELECT 1 FROM SYS_USER dp_user WHERE dp_user.DELETED = 0 AND dp_user.ID IN ("
                + createId + ", " + modifyId + ") AND " + departmentPredicate + ")";
    }

    private String validIds(Collection<Long> ids) {
        if (ids == null) {
            return "";
        }
        return ids.stream().filter(id -> id != null && id > 0L).distinct()
                .map(String::valueOf).collect(Collectors.joining(", "));
    }

    private boolean shouldIgnore(Table table, String statementId, DataPermission config) {
        String tableName = normalizeTableName(table.getName());
        if (INTERNAL_TABLES.contains(tableName) || containsIgnoreCase(config.getExcludeTables(), tableName)) {
            return true;
        }
        if (containsIgnoreCase(config.getExcludeStatements(), statementId)) {
            return true;
        }
        return this.ignoredStatementCache.computeIfAbsent(statementId, this::hasIgnoreAnnotation);
    }

    private boolean hasIgnoreAnnotation(String statementId) {
        int separator = statementId.lastIndexOf('.');
        if (separator <= 0 || separator == statementId.length() - 1) {
            return false;
        }
        String className = statementId.substring(0, separator);
        String methodName = statementId.substring(separator + 1);
        try {
            Class<?> mapperClass = Class.forName(className);
            if (mapperClass.isAnnotationPresent(Ignore.class)) {
                return true;
            }
            for (Method method : mapperClass.getMethods()) {
                if (methodName.equals(method.getName()) && method.isAnnotationPresent(Ignore.class)) {
                    return true;
                }
            }
        } catch (ClassNotFoundException exception) {
            log.debug("无法加载 Mapper 类型，按未忽略数据权限处理: {}", className);
        }
        return false;
    }

    private boolean containsIgnoreCase(List<String> values, String expected) {
        if (values == null || expected == null) {
            return false;
        }
        return values.stream().filter(value -> value != null).map(String::trim)
                .anyMatch(value -> value.equalsIgnoreCase(expected));
    }

    private String normalizeTableName(String tableName) {
        int separator = tableName.lastIndexOf('.');
        String simpleName = separator >= 0 ? tableName.substring(separator + 1) : tableName;
        return simpleName.replace("`", "").replace("\"", "").toLowerCase(Locale.ROOT);
    }
}
