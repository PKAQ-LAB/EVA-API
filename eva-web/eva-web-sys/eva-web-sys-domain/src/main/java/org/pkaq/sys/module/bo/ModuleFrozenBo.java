package org.pkaq.sys.module.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.SingleArray;

/**
 * 模块冻结状态修改参数。
 *
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModuleFrozenBo extends SingleArray<Long> {
    @NotNull
    @Schema(description = "目标冻结状态：0-启用，1-冻结")
    private Integer frozen;
}
