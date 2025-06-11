package org.pkaq.sys.role.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.bo.Bo;

/**
 * 角色模块关系表
 *
 * @author: S.PKAQ
 */
@Data
@Schema(title = "角色模块关系")
public class RoleModuleRefBo implements Bo {

    @Schema(description = "角色ID")
    private String roleId;

    @Schema(description = "模块ID")
    private String moduleId;

}
