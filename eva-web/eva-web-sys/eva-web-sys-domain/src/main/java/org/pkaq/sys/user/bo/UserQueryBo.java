package org.pkaq.sys.user.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.PageBo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PKAQ
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "用户列表查询Bo")
public class UserQueryBo extends PageBo {
    @Schema(description = "编号")
    private String code;

    @Schema(description = "电话")
    private String tel;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "账号")
    private String account;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "所属岗位ID")
    private List<String> postId = new ArrayList<>();

    @Schema(description = "所属部门")
    private String deptId;

}
