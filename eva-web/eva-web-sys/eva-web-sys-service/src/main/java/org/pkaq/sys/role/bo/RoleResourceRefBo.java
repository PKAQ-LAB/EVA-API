package org.pkaq.sys.role.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.bo.Bo;

import java.util.List;

/**
 * 角色模块关系表
 *
 * @author: S.PKAQ
 */
@Data
@Schema(title = "角色模块关系")
public class RoleResourceRefBo implements Bo {

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "资源ID")
    private List<Long> resourceId;

}
