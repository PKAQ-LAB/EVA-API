package org.pkaq.core.auth.adapter.mybatis.audit.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.spi.IOpenApiAuditAdministratorQuery;
import org.pkaq.core.constant.CommonConstant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 仅供集中审计读取核验资格，不复用缺少逻辑删除过滤的旧角色快照查询。
 * @author PKAQ
 * @date 2026-10-24
 */
@Repository
@RequiredArgsConstructor
public class OpenApiAuditAdministratorQuery implements IOpenApiAuditAdministratorQuery {
    private final JdbcTemplate jdbcTemplate;

    /** {@inheritDoc} */
    @Override
    public boolean isActiveAdministrator(long userId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM SYS_ACCOUNT account
                    JOIN SYS_ROLEUSER_REF membership ON membership.USER_ID = account.ID
                    JOIN SYS_ROLE role ON role.ID = membership.ROLE_ID
                    WHERE account.ID = ? AND account.TENANT_ID = 0 AND account.DELETED = 0
                      AND account.FROZEN IN (0, 9999)
                      AND role.TENANT_ID = 0 AND role.DELETED = 0 AND role.FROZEN IN (0, 9999)
                      AND role.CODE = ?
                )
                """, Boolean.class, userId, CommonConstant.ADMIN_ROLE_NAME));
    }
}
