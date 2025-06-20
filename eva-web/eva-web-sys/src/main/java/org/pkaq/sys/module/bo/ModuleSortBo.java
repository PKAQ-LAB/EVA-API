
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

    private String id;

    @Schema(description = "展示顺序")
    private int sort;
}
