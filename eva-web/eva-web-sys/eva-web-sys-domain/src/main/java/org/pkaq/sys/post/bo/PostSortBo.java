package org.pkaq.sys.post.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.Bo;

/**
 * 岗位顺序调整 BO（同级拖拽）
 *
 * @author dmz
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "岗位顺序调整Bo")
public class PostSortBo implements Bo {
    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "旧的顺序")
    private int oldSort;

    @Schema(description = "新的顺序")
    private int newSort;
}
