package org.pkaq.sys.tenant.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户管理-列表-授权参数
 *
 * @author 茂茂AdamEve
 */
@Schema(description = "租户管理-列表-授权参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantAuthBo {
    @NotBlank
    private String id;

    @Schema(description = "原角色id 修改时材有")
    private String oldRole;

    @Schema(description = "角色id")
    @NotBlank
    private String roleId;
}
