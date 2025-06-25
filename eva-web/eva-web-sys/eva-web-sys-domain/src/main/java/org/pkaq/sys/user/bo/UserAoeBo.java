package org.pkaq.sys.user.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.StdBo;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: S.PKAQ
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

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "账号")
    @NotBlank(message = "{sys.user.account.required}")
    private String account;

    @Schema(description = "密码")
    @NotBlank(message = "{sys.user.pwd.required}")
    private String password;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "最后登录ip")
    private String lastIp;

    @Schema(description = "最后登录时间")
    private Date lastLogin;

    @Schema(description = "所属岗位ID")
    private List<Long> postId = new ArrayList<>();

    @Schema(description = "所属部门")
    private String deptId;

    @Schema(description = "用户拥有的角色")
    private List<Long> roleIds = new ArrayList<>();

}
