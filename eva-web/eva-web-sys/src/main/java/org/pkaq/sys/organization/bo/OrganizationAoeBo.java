
package org.pkaq.sys.organization.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.StdBo;

import java.util.List;

/**
 * @author: S.PKAQ
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "组织管理编辑Bo")
public class OrganizationAoeBo extends StdBo {

    @Schema(description = "组织名称")
    @NotBlank
    private String name;

    @Schema(description = "编码")
    @NotBlank
    private String code;

    @Schema(description = "上级节点Id")
    private String pid;

    @Schema(description = "上级节点id路径")
    private String path;

    @Schema(description = "是否是叶子")
    private boolean isleaf;
}
