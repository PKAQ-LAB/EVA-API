package tech.yunyue.sys.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.yunyue.sys.post.consts.SYSConstant;
import tech.yunyue.sys.annotation.Code;

/**
 * @author dmz
 */
@Schema(description = "岗位管理列表视图对象")
@Data
@EqualsAndHashCode(callSuper = false)
public class PostTableVo {
    /**
     * 记录id
     */
    @Schema(description = "记录id")
    private String id;
    /**
     * 编码
     */
    @Schema(description = "编码")
    private String code;
    /**
     * 岗位
     */
    @Schema(description = "岗位")
    private String title;
    /**
     * 职级
     */
    @Schema(description = "职级")
    private String level;
    /**
     * 上级岗位ID
     */
    @Schema(description = "上级岗位ID")
    private String parentId;
    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;
    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer sorts;
    /**
     * 上级岗位名称
     */
    @Schema(description = "上级岗位名称")
    private String parentName;
}
