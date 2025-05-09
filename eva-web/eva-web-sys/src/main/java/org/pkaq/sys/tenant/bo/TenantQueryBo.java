package org.pkaq.sys.tenant.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.PageBo;

/**
 * 租户管理-列表-请求参数
 *
 * @author PKAQ
 */
@Schema(description = "租户管理-列表-请求参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantQueryBo extends PageBo {
    @Schema(description = "租户名称")
    private String name;

    @Schema(description = "租户编码")
    private String code;

    @Schema(description = "证件号")
    private String cardNo;

    @Schema(description = "联系人")
    private String contactName;
}
