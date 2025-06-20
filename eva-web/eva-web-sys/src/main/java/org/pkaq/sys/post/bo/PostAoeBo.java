package org.pkaq.sys.post.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.Bo;

/**
 * @author dmz
 */
@Schema(description = "岗位新增/编辑请求参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class PostAoeBo implements Bo {
    @Schema(description = "记录id")
    private String id;

    @Schema(description = "编码")
    @NotBlank(message = "岗位编码不能为null")
    @Size(max = 15)
    private String code;

    @Schema(description = "岗位")
    @NotBlank(message = "岗位名称不能为null")
    @Size(max = 30)
    private String title;

    @Schema(description = "职级")
    @Size(max = 6)
    private String level;

    @Schema(description = "上级id")
    private String pid;

    @Schema(description = "是否冻结")
    private String forzen;

    @Schema(description = "排序")
    private Integer sort;

    private Integer revision;

    @Schema(description = "备注")
    private String remark;
}
