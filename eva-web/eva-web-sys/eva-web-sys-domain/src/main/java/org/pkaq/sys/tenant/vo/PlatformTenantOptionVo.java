package org.pkaq.sys.tenant.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 平台跨租户查看的租户选项。
 *
 * @author PKAQ
 */
@Data
@AllArgsConstructor
public class PlatformTenantOptionVo {
    @Schema(description = "租户 ID")
    private Long id;

    @Schema(description = "租户编码")
    private String code;

    @Schema(description = "租户名称")
    private String name;
}
