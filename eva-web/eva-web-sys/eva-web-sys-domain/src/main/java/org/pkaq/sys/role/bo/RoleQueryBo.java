package org.pkaq.sys.role.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.PageBo;

/**
 * @author PKAQ
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "角色管理列表查询 bo")
public class RoleQueryBo extends PageBo {
    @Schema(description = "编码")
    private String code;

    @Schema(description = "名称")
    private String name;
}
