package org.pkaq.sys.organization.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.Bo;

/**
 * @author: S.PKAQ
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "组织管理顺序调整Bo")
public class OrganizationSortBo implements Bo {

    private String id;

    @Schema(description = "展示顺序")
    private int sort;
}
