package org.pkaq.sys.dict.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.pkaq.core.mvc.bo.Bo;

/**
 * @author PKAQ
 */
@Data
@Schema(title = "字典明细BO")
public class DictAoeLineBo implements Bo {
    @NotBlank(message = "主表ID不允许为空")
    @Schema(description = "主表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long mainId;

    @NotBlank(message = "字典项不允许为空")
    @Schema(description = "字典项键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dCode;

    @NotBlank(message = "字典值不允许为空")
    @Schema(description = "字典项值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dValue;
}
