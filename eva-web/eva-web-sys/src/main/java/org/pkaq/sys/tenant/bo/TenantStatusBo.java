package org.pkaq.sys.tenant.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户管理-修改租户状态-请求参数
 *
 * @author PKAQ
 */
@Schema(description = "租户管理-修改租户状态-请求参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantStatusBo {
    @NotBlank
    private String id;

    @Schema(description = "状态")
    @NotBlank
    @Size(max = 6)
    private String frozen;
}
