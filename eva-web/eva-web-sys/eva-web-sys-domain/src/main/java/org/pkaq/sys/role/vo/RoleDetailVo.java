package org.pkaq.sys.role.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.StdVo;

/**
 * @author PKAQ
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "角色詳情VO")
public class RoleDetailVo extends StdVo {
    @Schema(description = "编码")
    private String code;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "数据权限范围，字典类型 DATA_SCOPE")
    private String dataScope;

    @Schema(description = "自定义数据权限组织 ID，多个 ID 使用英文逗号分隔")
    private String dataOrgIds;
}
