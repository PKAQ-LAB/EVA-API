package org.pkaq.sys.tenant.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.Bo;

/**
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "租户编码/名称校验bo")
public class TenantCheckBo implements Bo {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "编号")
    private String code;

    @Schema(description = "名称")
    private String name;
}
