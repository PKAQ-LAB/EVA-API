package org.pkaq.sys.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.vo.Vo;

/**
 * @author PKAQ
 */
@Data
@Schema(title = "用户简要信息Vo")
public class UserSimpleVo implements Vo {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "编号")
    private String code;

    @Schema(description = "电话")
    private String tel;

    @Schema(description = "账号")
    private String account;

    @Schema(description = "姓名")
    private String name;

}
