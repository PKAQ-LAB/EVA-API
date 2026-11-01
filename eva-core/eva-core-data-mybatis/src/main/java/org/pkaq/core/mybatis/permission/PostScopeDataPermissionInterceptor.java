package org.pkaq.core.mybatis.permission;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;

/**
 * 沿用原数据权限插件，仅在实际SELECT解析期间登记命令身份。
 * @author PKAQ
 * @date 2026-11-01
 */
public class PostScopeDataPermissionInterceptor extends DataPermissionInterceptor {
    public PostScopeDataPermissionInterceptor(DataPermissionHandler handler) {
        super(handler);
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement statement, Object parameter,
                            RowBounds bounds, ResultHandler resultHandler, BoundSql sql) throws SQLException {
        if (statement.getSqlCommandType() != SqlCommandType.SELECT) {
            super.beforeQuery(executor, statement, parameter, bounds, resultHandler, sql);
            return;
        }
        try (PostReadScope.Window ignored = PostReadScope.select(statement.getId())) {
            super.beforeQuery(executor, statement, parameter, bounds, resultHandler, sql);
        }
    }
}
