package org.pkaq.sys.tenant.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户管理-列表-授权参数
 *
 * @author PKAQ
 */
@Schema(description = "租户管理-列表-授权参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantAuthBo {
    @NotNull
    private Long id;

    @Schema(description = "原角色 id，修改时使用")
    private String oldRole;

    @Schema(description = "角色id")
    @NotNull
    private Long roleId;
}
