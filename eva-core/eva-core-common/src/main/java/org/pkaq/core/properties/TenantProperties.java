package org.pkaq.core.properties;

import lombok.Data;

/**
 * 双运行模式租户配置。
 *
 * @author PKAQ
 */
@Data
public class TenantProperties {
    public static final String MODE_STANDALONE = "standalone";
    public static final String MODE_SCHEMA = "schema";

    /** 是否启用 schema 租户模式。 */
    private boolean enable;

    /** 运行模式；enable=false 时强制按 standalone 处理。 */
    private String mode = MODE_STANDALONE;

    /** 平台控制面 schema。 */
    private String coreSchema = "eva_core";

    /** 单体模式使用的唯一 schema。 */
    private String standaloneSchema = "eva";

    /** 租户 schema 名称前缀。 */
    private String prefix = "tenant_";

    public boolean isSchemaMode() {
        return isEnable() && MODE_SCHEMA.equalsIgnoreCase(mode);
    }
}
