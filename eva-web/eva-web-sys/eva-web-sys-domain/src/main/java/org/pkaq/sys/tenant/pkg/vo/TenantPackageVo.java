package org.pkaq.sys.tenant.pkg.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.StdVo;

import java.util.Set;

/**
 * 租户套餐视图对象。
 *
 * @author PKAQ
 */
@Schema(description = "租户套餐视图对象")
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantPackageVo extends StdVo {

    @Schema(description = "套餐编码")
    private String code;

    @Schema(description = "套餐名称")
    private String name;

    @Schema(description = "授权用户数")
    private int authUserCount;

    @Schema(description = "有效天数")
    private int validDays;

    @Schema(description = "授权资源ID集合")
    private Set<Long> resourceIds;
}
