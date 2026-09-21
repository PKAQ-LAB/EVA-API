package org.pkaq.sys.organization.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.SingleArray;

/**
 * 组织冻结状态修改参数。
 *
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrganizationFrozenBo extends SingleArray<Long> {
    @NotNull
    @Schema(description = "目标冻结状态：0-启用，1-冻结")
    private Integer frozen;
}
