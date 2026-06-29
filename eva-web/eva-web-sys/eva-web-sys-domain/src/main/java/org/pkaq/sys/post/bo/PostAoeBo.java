package org.pkaq.sys.post.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.Bo;

/**
 * 岗位新增 / 编辑请求参数
 * <p>
 * 注意：frozen 走独立的 /switch 接口，sort 走 /sort 接口，本 BO 不接收这两个字段，避免前端越权修改排序或冻结状态。
 * @author dmz
 */
@Schema(description = "岗位新增/编辑请求参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class PostAoeBo implements Bo {
    @Schema(description = "璁板綍id")
    private Long id;

    @Schema(description = "编码")
    @NotBlank(message = "岗位编码不能为null")
    @Size(max = 15)
    private String code;

    @Schema(description = "岗位名称")
    @NotBlank(message = "岗位名称不能为null")
    @Size(max = 30)
    private String title;

    @Schema(description = "鑱岀骇")
    @Size(max = 6)
    private String level;

    @Schema(description = "上级岗位 ID，根节点传 0 或留空")
    private Long pid;

    @Schema(description = "乐观锁版本号")
    private Integer revision;

    @Schema(description = "备注")
    private String remark;
}
