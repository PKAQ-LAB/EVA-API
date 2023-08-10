package tech.yunyue.interceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import tech.yunyue.core.threaduser.ThreadUserHelper;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 往mybatis的param中动态增加全局参数
 */
public class GlobalParamsInterceptor implements InnerInterceptor {
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        addParameter(boundSql);
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
        addParameter(ms.getBoundSql(parameter));
    }

    @Override
    public void beforeGetBoundSql(StatementHandler sh) {
        addParameter(sh.getBoundSql());
    }

    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        addParameter(sh.getBoundSql());
    }

    /**
     * 增加全局参数 tenant 在xml文件中可使用#{tenant}得到租户id
     * @param boundSql
     */
    protected void addParameter(BoundSql boundSql) {
        if (boundSql != null) boundSql.setAdditionalParameter("tenant", ThreadUserHelper.getTenantId());

    }
}
