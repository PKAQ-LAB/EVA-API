package org.pkaq.core.mybatis.tenant;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 使用事务级 search_path 路由租户私有表。
 *
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class TenantSchemaRouter {
    private final DataSource dataSource;
    private final EvaConfig evaConfig;
    private final TenantSchemaResolver resolver;

    public String routeCurrentTransaction() {
        if (!evaConfig.getTenant().isSchemaMode()) {
            return null;
        }
        return routeCurrentTransaction(ThreadUserHelper.getTenantId());
    }

    /**
     * 根据服务端已验证的租户 ID 设置当前事务路由。
     *
     * @param tenantId 服务端租户 ID
     */
    public String routeCurrentTransaction(Long tenantId) {
        if (!evaConfig.getTenant().isSchemaMode()) {
            return null;
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("租户 schema 路由必须位于事务内");
        }
        String schema = resolver.resolve(tenantId);
        String searchPath = schema + "," + evaConfig.getTenant().getCoreSchema();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        String previous = readSearchPath(connection);
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT pg_catalog.set_config('search_path', ?, true)")) {
            statement.setString(1, searchPath);
            statement.execute();
            TenantContext.bind(tenantId, schema);
            return previous;
        } catch (SQLException exception) {
            throw new IllegalStateException("设置租户事务 schema 失败", exception);
        }
    }

    public void restoreCurrentTransaction(String searchPath) {
        if (!evaConfig.getTenant().isSchemaMode() || searchPath == null) {
            return;
        }
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT pg_catalog.set_config('search_path', ?, true)")) {
            statement.setString(1, searchPath);
            statement.execute();
        } catch (SQLException exception) {
            throw new IllegalStateException("恢复事务 schema 失败", exception);
        }
    }

    private String readSearchPath(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT current_setting('search_path')");
             var resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getString(1);
        } catch (SQLException exception) {
            throw new IllegalStateException("读取事务 schema 失败", exception);
        }
    }
}
