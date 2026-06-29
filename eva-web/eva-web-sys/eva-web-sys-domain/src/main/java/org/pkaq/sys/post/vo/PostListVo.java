package org.pkaq.sys.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.StdTreeVo;

/**
 * 岗位列表视图对象（树形）
 *
 * @author dmz
 */
@Schema(description = "岗位管理列表视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class PostListVo extends StdTreeVo {
    @Schema(description = "岗位名称")
    private String title;

    @Schema(description = "职级")
    private String level;

    @Schema(description = "上级岗位名称")
    private String parentTitle;
}
