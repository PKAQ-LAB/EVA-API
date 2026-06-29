package org.pkaq.sys.tenant.pkg.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.PageBo;

/**
 * 租户套餐查询参数。
 *
 * @author PKAQ
 */
@Schema(description = "租户套餐查询参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantPackageQueryBo extends PageBo {

    @Schema(description = "套餐编码")
    private String code;

    @Schema(description = "套餐名称")
    private String name;
}
