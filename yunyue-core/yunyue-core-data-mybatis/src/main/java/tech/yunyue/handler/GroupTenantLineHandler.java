package tech.yunyue.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.threaduser.ThreadUserHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 集团租户插件
 */
@Component
public class GroupTenantLineHandler implements TenantLineHandler {
    @Autowired
    EvaConfig evaConfig;

    /**
     * 获取租户 ID 值表达式，只支持单个 ID 值
     * @return 租户 ID 值表达式
     */
    @Override
    public Expression getTenantId() {
        String tenantId = ThreadUserHelper.getTenantId();
        if(StringUtils.isEmpty(tenantId)) {
            tenantId = evaConfig.getTenant().getDefaultTenantId();
        }
        return new StringValue(tenantId);
    }

    /**
     * 获取租户字段名
     * 默认字段名叫: tenant_id
     * @return 租户字段名
     */
    @Override
    public String getTenantIdColumn() {
        return evaConfig.getTenant().getTenantId();
    }

    /**
     * 根据表名判断是否忽略拼接多租户条件
     * 默认都要进行解析并拼接多租户条件
     * <p>
     * 如果有部分 sql 不需要加上租户ID条件 可以使用 @InterceptorIgnore(tenantLine = "true") 标注在 Mapper 接口的方法上
     * @param tableName 表名
     * @return 是否忽略, true:表示忽略，false:需要解析并拼接多租户条件
     */
    @Override
    public boolean ignoreTable(String tableName) {
        //防止匿名用户
        if(Objects.isNull(ThreadUserHelper.getTenantId())) return true;
        String[] tableNames = evaConfig.getTenant().getIgnoreTables();
        if(null == tableNames || tableNames.length == 0) return false;

        //返回true就不拼接
        return Arrays.stream(tableNames).anyMatch(((name) -> name.equalsIgnoreCase(tableName)));
    }

    /**
     * 已给出租户列的 insert 不再拼接条件。使用用户给出的值。
     * 针对比较特殊的场景，比如：异步添加时，获取不到登录人的租户ID，则给默认租户ID
     * @param columns
     * @param tenantIdColumn
     * @return
     */
    @Override
    public boolean ignoreInsert(List<Column> columns, String tenantIdColumn) {
        // 返回true就不拼接
        return TenantLineHandler.super.ignoreInsert(columns, tenantIdColumn);
    }
}
