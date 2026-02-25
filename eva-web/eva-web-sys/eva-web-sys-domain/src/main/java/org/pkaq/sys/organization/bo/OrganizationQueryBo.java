package org.pkaq.sys.organization.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.StdBo;

/**
 * @author PKAQ
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "组织管理列表查询BO")
public class OrganizationQueryBo extends StdBo {

    @Schema(description = "组织名称")
    private String name;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "上级节点Id")
    private String parentId;
}
