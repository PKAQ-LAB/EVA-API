package tech.yunyue.handler;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.threaduser.ThreadUserHelper;

import java.util.Objects;

/**
 * 公司租户插件
 */
@Component
public class CompanyTenantLineHandler extends GroupTenantLineHandler {
    @Autowired
    EvaConfig evaConfig;

    /**
     * 获取租户 ID 值表达式，只支持单个 ID 值
     * @return 租户 ID 值表达式
     */
    @Override
    public Expression getTenantId() {
        String tenantId = ThreadUserHelper.getComTenantId();
        return new StringValue(tenantId);
    }

    /**
     * 获取租户字段名
     * 默认字段名叫: tenant_company_id
     * @return 租户字段名
     */
    @Override
    public String getTenantIdColumn() {
        return evaConfig.getTenant().getComTenantId();
    }

    /**
     * 当前用户为集团用户时 不拼接该处理器
     * @param tableName 表名
     * @return
     */
    @Override
    public boolean ignoreTable(String tableName) {
        //没有公司id的即集团用户 查询范围是整个集团
        if(Objects.isNull(ThreadUserHelper.getComTenantId())) return false;
        return super.ignoreTable(tableName);
    }
}
