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
    private boolean enable = true;
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
     */
    private String[] ignoreTables;
}
