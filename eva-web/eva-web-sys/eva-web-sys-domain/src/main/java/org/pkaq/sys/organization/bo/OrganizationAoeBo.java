package org.pkaq.sys.organization.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.StdBo;

/**
 * 组织管理新增 / 编辑请求参数
 * <p>
 * 注意：frozen 走独立的 /switch 接口，sort/path/isleaf 由 Service 维护，本 BO 不接收这些字段，避免前端越权修改排序、冻结状态或破坏 path 链。
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "组织管理编辑Bo")
public class OrganizationAoeBo extends StdBo {

    @Schema(description = "组织名称")
    @NotBlank
    private String name;

    @Schema(description = "编码")
    @NotBlank
    private String code;

    @Schema(description = "上级节点 ID，根节点传 0 或留空")
    private Long pid;
}
