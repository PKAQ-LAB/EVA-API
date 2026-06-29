package org.pkaq.sys.tenant.pkg.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.StdBo;

import java.util.List;

/**
 * 租户套餐新增编辑参数。
 *
 * @author PKAQ
 */
@Schema(description = "租户套餐新增编辑参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantPackageAoeBo extends StdBo {

    @Schema(description = "套餐名称")
    @NotBlank
    @Size(max = 100, message = "{validate.length.max}")
    private String name;

    @Schema(description = "授权用户数")
    @Min(value = 1)
    private int authUserCount;

    @Schema(description = "有效天数")
    @Min(value = 1)
    private int validDays;

    @Schema(description = "授权资源ID集合")
    @NotNull
    private List<Long> resourceIds;
}
