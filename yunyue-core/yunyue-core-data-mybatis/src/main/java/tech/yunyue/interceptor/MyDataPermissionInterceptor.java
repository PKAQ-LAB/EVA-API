package tech.yunyue.interceptor;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import lombok.*;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;

/**
 * 由于 @InterceptorIgnore默认关闭数据权限 @InterceptorIgnore(tenantLine = "true") 就直接不走数据权限校验
 * 所以重写beforeQuery() 系统中只能使用 @Ignore忽略数据权限
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MyDataPermissionInterceptor extends DataPermissionInterceptor {
    private DataPermissionHandler dataPermissionHandler;
    public MyDataPermissionInterceptor (DataPermissionHandler dataPermissionHandler) {
        super(dataPermissionHandler);
        this.dataPermissionHandler = dataPermissionHandler;
    }
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
        mpBs.sql(parserSingle(mpBs.sql(), ms.getId()));
    }
}
