package org.pkaq.sys.tenant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 租户授权资源关系。
 *
 * @author PKAQ
 */
@Data
@Alias("tenantResource")
@TableName("SYS_TENANT_RESOURCE")
public class TenantResourceEntity {
    private Long tenantId;

    private Long resourceId;
}
