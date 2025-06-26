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
}
