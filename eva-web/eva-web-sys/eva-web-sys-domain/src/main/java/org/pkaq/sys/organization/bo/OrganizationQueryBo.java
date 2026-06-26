package org.pkaq.sys.organization.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.Bo;

/**
 * 组织管理树形查询 BO
 *
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "组织管理查询BO")
public class OrganizationQueryBo implements Bo {

    @Schema(description = "节点 ID")
    private Long id;

    @Schema(description = "组织名称")
    private String name;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "上级节点 ID（按层级筛选）")
    private Long pid;
}
