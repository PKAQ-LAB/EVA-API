package org.pkaq.core.auth.adapter.mybatis.audit.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mybatis.log.LoginLogQueryScope;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 登录日志独立事务及唯一物理来源；提交异常由调用方在代理外捕获。
 * @author PKAQ
 * @date 2026-10-30
 */
@Service
@RequiredArgsConstructor
public class LoginLogStorage {
    private final EvaConfig evaConfig;
    private final CoreSchemaExecutor coreSchemaExecutor;
    private final JdbcTemplate jdbcTemplate;

    /** 一个审计动作使用独立短事务，不污染调用者事务。 */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void write(Runnable action) {
        inSource(() -> {
            LoginLogQueryScope.write(action);
            return null;
        });
    }

    /** 资格与历史读取在独立只读事务内使用同一来源。 */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = Exception.class)
    public <T> T read(Supplier<T> action) {
        return inSource(action);
    }

    /** 拒绝临时表、缺表及 search_path 回退到其它 schema。 */
    public void requireRelations(String schema, String... relations) {
        if (schema == null || !schema.matches("[a-z_][a-z0-9_]{0,62}")
                || !TransactionSynchronizationManager.isActualTransactionActive()) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        Set<String> found = new HashSet<>();
        for (String relation : relations) {
            Map<String, Object> source = jdbcTemplate.queryForMap("""
                    SELECT pg_catalog.current_schema() AS current_schema_name,
                        (SELECT n.nspname FROM pg_catalog.pg_class c
                         JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                         WHERE c.oid = pg_catalog.to_regclass(?)) AS relation_schema_name
                    """, relation);
            if (!schema.equals(source.get("current_schema_name"))
                    || !schema.equals(source.get("relation_schema_name")) || !found.add(relation)) {
                CommonCodes.PERMISSION_DENY.newException();
            }
        }
    }

    private <T> T inSource(Supplier<T> action) {
        boolean schemaMode = evaConfig.getTenant().isSchemaMode();
        if (!schemaMode && !evaConfig.isStandaloneMode()
                || schemaMode && !evaConfig.isPlatformMode() && !evaConfig.isSaasMode()) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        String schema = schemaMode ? evaConfig.getTenant().getCoreSchema()
                : evaConfig.getTenant().getStandaloneSchema();
        Supplier<T> operation = () -> {
            requireRelations(schema, "sys_login_log");
            Long previousTenant = TenantContext.tenantId();
            String previousSchema = TenantContext.schemaName();
            TenantContext.bind(0L, schema);
            try {
                return action.get();
            } finally {
                if (previousTenant == null && previousSchema == null) {
                    TenantContext.clear();
                } else {
                    TenantContext.bind(previousTenant, previousSchema);
                }
            }
        };
        return schemaMode ? coreSchemaExecutor.execute(jdbc -> operation.get()) : operation.get();
    }
}
