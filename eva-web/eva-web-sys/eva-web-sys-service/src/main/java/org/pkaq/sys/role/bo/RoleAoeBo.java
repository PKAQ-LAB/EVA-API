package org.pkaq.sys.role.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.StdBo;

/**
 * @author PKAQ
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "角色管理AOE bo")
public class RoleAoeBo extends StdBo {
    @Schema(description = "编码")
    private String code;

    @Schema(description = "名称")
    private String name;
}
