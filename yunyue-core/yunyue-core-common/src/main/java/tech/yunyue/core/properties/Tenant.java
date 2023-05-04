package tech.yunyue.core.properties;

import lombok.Data;

import java.util.List;

/**
 * 租户配置
 */
@Data
public class Tenant {
    /**
     * tenant header参数名
     **/
    private String header = "yy-tenant-id";
    /**
     * 是否开启租户模式
     */
    private boolean enable = false;
    /**
     * 默认租户id
     */
    private String defaultTenantId="0";
    /**
     * 租户字段名
     */
    private String tenantIdColumn = "TENANT_ID";
    /**
     * 不需要拼接租户字段的表
     * <p>
     * 如果有部分 sql 不需要加上租户ID条件 可以使用 @InterceptorIgnore(tenantLine = "true") 标注在 Mapper 接口的方法上
     */
    private String[] ignoreTables;
}
