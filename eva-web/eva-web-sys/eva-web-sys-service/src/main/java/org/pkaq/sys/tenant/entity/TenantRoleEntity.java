package org.pkaq.sys.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * 租户获得的平台角色模板授权。
 *
 * @author PKAQ
 */
@Data
@Alias("tenantUser")
@TableName("SYS_TENANT_ROLE")
@EqualsAndHashCode()
public class TenantRoleEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @NotNull
    private Long tenantId;

    @NotNull
    private Long roleId;

    private Boolean active;

    private LocalDateTime grantedAt;

    private LocalDateTime revokedAt;

    private String statusReason;
}
