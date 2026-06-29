package org.pkaq.sys.user.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.StdBo;

import java.sql.Date;
import java.util.List;

/**
 * 用户新增编辑参数
 *
 * @author PKAQ
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "用户管理编辑Bo")
public class UserAoeBo extends StdBo {

    @Schema(description = "编号")
    @NotBlank(message = "{sys.user.code.required}")
    private String code;

    @Schema(description = "电话")
    private String tel;

    @Schema(description = "閭")
    private String email;

    @Schema(description = "璐﹀彿")
    @NotBlank(message = "{sys.user.account.required}")
    private String account;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "濮撳悕")
    private String name;

    @Schema(description = "鏄电О")
    private String nickName;

    @Schema(description = "最后登录 IP")
    private String lastIp;

    @Schema(description = "最后登录时间")
    private Date lastLogin;

    @Schema(description = "所属岗位 ID")
    private List<Long> postId;

    @Schema(description = "所属部门")
    private Long deptId;

    @Schema(description = "用户拥有的角色")
    private List<Long> roleIds;
}
