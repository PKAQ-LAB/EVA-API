package org.pkaq.core.auth.adapter.mybatis.role.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 岗位资格只查询当前认证来源，显式schema防止同名表回落。
 * @author PKAQ
 * @date 2026-11-01
 */
@Component
@RequiredArgsConstructor
public class MybatisPostEligibilityQuery {
    private final JdbcTemplate jdbcTemplate;
    private final EvaConfig evaConfig;

    /** 同一查询核有效账号、角色、角色成员、岗位成员及配置，不查询业务记录作者。 */
    public Map<Long, List<Long>> find(Long userId) {
        String schema;
        if (evaConfig.getTenant().isSchemaMode()) {
            schema = TenantContext.schemaName();
            if (TenantContext.tenantId() == null || TenantContext.tenantId() <= 0L) {
                return Map.of();
            }
        } else if (evaConfig.isStandaloneMode() && !evaConfig.getTenant().isEnable()) {
            schema = evaConfig.getTenant().getStandaloneSchema();
        } else {
            return Map.of();
        }
        if (userId == null || userId <= 0L || schema == null || !schema.matches("[a-z_][a-z0-9_]{0,62}")) {
            return Map.of();
        }
        String prefix = "\"" + schema + "\".";
        String sql = "SELECT r.id,r.data_post_ids,p.id AS post_id FROM " + prefix + "sys_role r JOIN "
                + prefix + "sys_roleuser_ref ru ON ru.role_id=r.id JOIN " + prefix
                + "sys_account a ON a.id=ru.user_id JOIN " + prefix
                + "sys_postuser_ref pu ON pu.user_id=a.id JOIN " + prefix + "sys_post p ON p.id=pu.post_id"
                + " WHERE a.id=? AND a.deleted=0 AND a.frozen IN (0,9999)"
                + " AND r.data_scope='0006' AND r.deleted=0 AND r.frozen IN (0,9999)"
                + " AND p.deleted=0 AND p.frozen IN (0,9999)"
                + (evaConfig.getTenant().isSchemaMode() ? "" : " AND (a.tenant_id IS NULL OR a.tenant_id=0)"
                    + " AND (r.tenant_id IS NULL OR r.tenant_id=0) AND (p.tenant_id IS NULL OR p.tenant_id=0)"
                    + " AND (pu.tenant_id IS NULL OR pu.tenant_id=0)");
        return jdbcTemplate.execute((ConnectionCallback<Map<Long, List<Long>>>) connection -> {
            try (var current = connection.prepareStatement("SELECT current_schema()");
                 var source = current.executeQuery()) {
                if (!source.next() || !schema.equals(source.getString(1))) {
                    return Map.of();
                }
            }
            Map<Long, List<Long>> configured = new HashMap<>();
            Set<Long> effective = new HashSet<>();
            try (var query = connection.prepareStatement(sql)) {
                query.setLong(1, userId);
                try (var rows = query.executeQuery()) {
                    while (rows.next()) {
                        configured.put(rows.getLong("id"), parse(rows.getString("data_post_ids")));
                        effective.add(rows.getLong("post_id"));
                    }
                }
            }
            configured.entrySet().removeIf(entry -> entry.getValue().stream().noneMatch(effective::contains));
            return Map.copyOf(configured);
        });
    }

    /** 非规范或不完整历史集合整体失效，禁止删掉坏ID后继续放行。 */
    private List<Long> parse(String csv) {
        if (csv == null || !csv.matches("[1-9][0-9]*(,[1-9][0-9]*)*")) {
            return List.of();
        }
        try {
            List<Long> ids = Arrays.stream(csv.split(",")).map(Long::valueOf).toList();
            return ids.stream().distinct().count() == ids.size() ? ids : List.of();
        } catch (NumberFormatException exception) {
            return List.of();
        }
    }
}
