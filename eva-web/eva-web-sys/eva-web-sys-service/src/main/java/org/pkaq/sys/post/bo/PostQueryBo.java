package org.pkaq.sys.post.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.PageBo;

/**
 * @author dmz
 */
@Schema(description = "岗位列表请求参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class PostQueryBo extends PageBo {
    @Schema(description = "岗位")
    private String title;

    @Schema(description = "职级")
    private String level;

    @Schema(description = "租户id")
    private String tenantId;

}
