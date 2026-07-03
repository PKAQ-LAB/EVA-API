package org.pkaq.sys.role.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.StdVo;

/**
 * 角色列表vo
 *
 * @author PKAQ
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoleListVo extends StdVo {
    @Schema(description = "编码")
    private String code;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "数据权限范围，字典类型 DATA_SCOPE")
    private String dataScope;
}
