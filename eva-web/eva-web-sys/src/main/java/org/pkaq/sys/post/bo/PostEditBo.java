package org.pkaq.sys.post.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "岗位编码不能为null")
    @Size(max = 15)
    private String code;
    /**
     * 岗位
     */
    @Schema(description = "岗位")
    @NotBlank(message = "岗位名称不能为null")
    @Size(max = 30)
    private String title;
    /**
     * 职级
     */
    @Schema(description = "职级")
    @Size(max = 6)
    private String level;
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
