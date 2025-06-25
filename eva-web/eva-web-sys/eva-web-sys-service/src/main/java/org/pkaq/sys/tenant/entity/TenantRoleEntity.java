package org.pkaq.sys.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

/**
 * 租户角色关系表
 * @author PKAQ
 */
@Data
@Alias("tenantUser")
@TableName("sys_tenant_role")
@EqualsAndHashCode()
public class TenantRoleEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @NotBlank
    private Long tenantId;

    @NotBlank
    private Long roleId;
}
