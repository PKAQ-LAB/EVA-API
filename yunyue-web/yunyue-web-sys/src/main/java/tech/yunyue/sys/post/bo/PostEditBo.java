package tech.yunyue.sys.post.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author dmz
 */
@Schema(description = "岗位新增/编辑请求参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class PostEditBo {
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
     * 乐观锁
     */
    private Integer revision;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
