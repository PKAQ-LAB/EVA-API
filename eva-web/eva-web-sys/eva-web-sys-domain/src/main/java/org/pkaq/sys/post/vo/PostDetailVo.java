package org.pkaq.sys.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.StdVo;

/**
 * 岗位详情视图对象
 *
 * @author dmz
 */
@Schema(description = "岗位管理详情视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class PostDetailVo extends StdVo {
    @Schema(description = "编码")
    private String code;

    @Schema(description = "岗位名称")
    private String title;

    @Schema(description = "职级")
    private String level;

    @Schema(description = "上级岗位ID")
    private Long pid;

    @Schema(description = "上级岗位名称")
    private String parentTitle;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "是否叶子节点")
    private Boolean isleaf;
}
