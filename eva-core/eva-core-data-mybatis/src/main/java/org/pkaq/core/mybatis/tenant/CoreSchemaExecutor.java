package org.pkaq.core.mybatis.tenant;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.function.Function;

/**
 * 在租户事务中显式访问平台 core 控制面。
 *
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class CoreSchemaExecutor {
    private final JdbcTemplate jdbcTemplate;
    private final EvaConfig evaConfig;

    public <T> T execute(Function<JdbcTemplate, T> callback) {
        if (!evaConfig.getTenant().isSchemaMode()) {
            return callback.apply(jdbcTemplate);
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("core schema 访问必须位于租户事务内");
        }
        String previous = jdbcTemplate.queryForObject("SELECT current_setting('search_path')", String.class);
        try {
            jdbcTemplate.queryForObject("SELECT pg_catalog.set_config('search_path', ?, true)",
                    String.class, evaConfig.getTenant().getCoreSchema());
            return callback.apply(jdbcTemplate);
        } finally {
            jdbcTemplate.queryForObject("SELECT pg_catalog.set_config('search_path', ?, true)",
                    String.class, previous);
        }
    }
}
