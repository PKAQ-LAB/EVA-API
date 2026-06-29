package org.pkaq.sys.tenant.pkg.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 租户套餐授权资源关系。
 *
 * @author PKAQ
 */
@Data
@Alias("tenantPackageResource")
@TableName("SYS_TENANT_PACKAGE_RESOURCE")
public class TenantPackageResourceEntity {
    private Long packageId;

    private Long resourceId;
}
