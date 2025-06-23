package org.pkaq.sys.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.bo.Bo;

import java.sql.Date;

/**
 * 用户管理实体类
 *
 * @author: S.PKAQ
 */
@Data
@Schema(title = "用户管理编辑Bo")
public class UserListVo implements Bo {

    @Schema(description = "ID")
    private String id;

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

    @Schema(description = "所属岗位")
    private String postId;

    @Schema(description = "所属部门")
    private String deptId;

}
