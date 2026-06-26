package org.pkaq.sys.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.StdVo;

/**
 * 组织管理详情视图对象
 *
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "组织管理详情Vo")
public class OrganizationDetailVo extends StdVo {

    @Schema(description = "组织名称")
    private String name;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "上级节点ID")
    private Long pid;

    @Schema(description = "上级节点名称")
    private String parentName;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "是否叶子节点")
    private Boolean isleaf;
}
