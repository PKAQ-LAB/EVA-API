package org.pkaq.sys.tenant.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.mybatis.tenant.TargetTenantExecutor;
import org.pkaq.core.util.BCryptUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 在平台事务内维护目标租户 schema 的账号与本地授权数据。
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class TenantPrivateAccountService {
    private final JdbcTemplate jdbcTemplate;
    private final TargetTenantExecutor targetTenantExecutor;

    public void createAdministrator(Long tenantId, Long adminId, String account, String password) {
        targetTenantExecutor.execute(tenantId, () -> {
            jdbcTemplate.update("""
                    INSERT INTO SYS_USER(
                        ID, REVISION, DELETED, FROZEN, SORT, UTC_CREATE,
                        CODE, ACCOUNT, PASSWORD, NAME, NICK_NAME, PERM_VER)
                    VALUES (?, 0, 0, 9999, 0, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, 0)
                    """, adminId, account, account, BCryptUtils.hashpw(password), account, account);
            return null;
        });
    }

    public String findAdministratorAccount(Long tenantId, Long adminId) {
        return targetTenantExecutor.execute(tenantId, () -> jdbcTemplate.query("""
                SELECT ACCOUNT FROM SYS_USER WHERE ID = ? AND COALESCE(DELETED, 0) = 0
                """, resultSet -> resultSet.next() ? resultSet.getString(1) : null, adminId));
    }

    public Set<Long> incrementAllPermVersions(Long tenantId) {
        return targetTenantExecutor.execute(tenantId, () -> {
            Set<Long> userIds = activeUserIds();
            jdbcTemplate.update("""
                    UPDATE SYS_USER SET PERM_VER = COALESCE(PERM_VER, 0) + 1
                    WHERE COALESCE(DELETED, 0) = 0
                    """);
            return userIds;
        });
    }

    public Set<Long> enforceUserLimit(Long tenantId, Long adminId, int userLimit) {
        return targetTenantExecutor.execute(tenantId, () -> {
            int limit = Math.max(userLimit, 0);
            List<Long> overflow = jdbcTemplate.queryForList("""
                    SELECT ID FROM SYS_USER
                    WHERE ID <> ? AND COALESCE(DELETED, 0) = 0 AND COALESCE(FROZEN, 0) = 0
                    ORDER BY UTC_CREATE ASC NULLS FIRST, ID ASC
                    OFFSET ?
                    """, Long.class, adminId, limit);
            for (Long userId : overflow) {
                jdbcTemplate.update("""
                        UPDATE SYS_USER SET FROZEN = 1, PERM_VER = COALESCE(PERM_VER, 0) + 1
                        WHERE ID = ? AND COALESCE(DELETED, 0) = 0 AND COALESCE(FROZEN, 0) = 0
                        """, userId);
            }
            return new HashSet<>(overflow);
        });
    }

    public Set<Long> softDeleteTenantAccounts(Long tenantId) {
        return targetTenantExecutor.execute(tenantId, () -> {
            Set<Long> userIds = activeUserIds();
            jdbcTemplate.update("DELETE FROM SYS_POSTUSER_REF");
            jdbcTemplate.update("DELETE FROM SYS_ROLEUSER_REF");
            jdbcTemplate.update("DELETE FROM SYS_ROLERES_REF");
            jdbcTemplate.update("UPDATE SYS_ROLE SET DELETED = ID WHERE COALESCE(DELETED, 0) = 0");
            jdbcTemplate.update("""
                    UPDATE SYS_USER
                    SET DELETED = ID, PERM_VER = COALESCE(PERM_VER, 0) + 1
                    WHERE COALESCE(DELETED, 0) = 0
                    """);
            return userIds;
        });
    }

    public Set<Long> pruneRoleResources(Long tenantId, Set<Long> allowedResourceIds) {
        return targetTenantExecutor.execute(tenantId, () -> {
            Set<Long> roleIds = new HashSet<>(jdbcTemplate.queryForList("""
                    SELECT ID FROM SYS_ROLE WHERE COALESCE(DELETED, 0) = 0
                    """, Long.class));
            List<Long> granted = jdbcTemplate.queryForList(
                    "SELECT DISTINCT RESOURCE_ID FROM SYS_ROLERES_REF", Long.class);
            for (Long resourceId : granted) {
                if (!allowedResourceIds.contains(resourceId)) {
                    jdbcTemplate.update("DELETE FROM SYS_ROLERES_REF WHERE RESOURCE_ID = ?", resourceId);
                }
            }
            return roleIds;
        });
    }

    public Set<Long> listActiveUserIds(Long tenantId) {
        return targetTenantExecutor.execute(tenantId, this::activeUserIds);
    }

    private Set<Long> activeUserIds() {
        return new HashSet<>(jdbcTemplate.queryForList("""
                SELECT ID FROM SYS_USER WHERE COALESCE(DELETED, 0) = 0
                """, Long.class));
    }
}
