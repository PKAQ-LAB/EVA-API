package org.pkaq.sys.module.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.Bo;

/**
 * @author: S.PKAQ
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "模块顺序调整Bo")
public class ModuleSortBo implements Bo {

    private Long id;

    @Schema(description = "旧的顺序")
    private int oldSort;

    @Schema(description = "新的顺序")
    private int newSort;
}
