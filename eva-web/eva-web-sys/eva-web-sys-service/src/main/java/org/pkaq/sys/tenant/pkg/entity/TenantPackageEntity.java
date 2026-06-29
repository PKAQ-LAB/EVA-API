package org.pkaq.sys.tenant.pkg.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

/**
 * 租户套餐实体。
 *
 * @author PKAQ
 */
@Data
@Alias("tenantPackageEntity")
@TableName("SYS_TENANT_PACKAGE")
@EqualsAndHashCode(callSuper = true)
public class TenantPackageEntity extends StdEntity {

    @TableField(condition = SqlCondition.LIKE)
    private String code;

    @TableField(condition = SqlCondition.LIKE)
    private String name;

    private int authUserCount;

    private int validDays;
}
