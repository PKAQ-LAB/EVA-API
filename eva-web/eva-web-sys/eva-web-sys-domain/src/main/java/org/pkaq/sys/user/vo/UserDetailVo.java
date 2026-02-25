package org.pkaq.sys.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.StdVo;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * @author PKAQ
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "用户管理详情Vo")
public class UserDetailVo extends StdVo {

    @Schema(description = "编号")
    private String code;

    @Schema(description = "电话")
    private String tel;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "账号")
    private String account;

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
    private Long deptId;

    @Schema(description = "用户拥有的角色")
    private List<Long> roleIds = new ArrayList<>();

}
