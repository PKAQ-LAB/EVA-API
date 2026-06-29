package org.pkaq.sys.user.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.PageBo;

import java.util.List;

/**
 * 用户列表查询参数
 *
 * @author PKAQ
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "用户列表查询 Bo")
public class UserQueryBo extends PageBo {
    @Schema(description = "编号")
    private String code;

    @Schema(description = "电话")
    private String tel;

    @Schema(description = "閭")
    private String email;

    @Schema(description = "璐﹀彿")
    private String account;

    @Schema(description = "濮撳悕")
    private String name;

    @Schema(description = "状态")
    private Integer frozen;

    @Schema(description = "所属岗位 ID")
    private List<Long> postId;

    @Schema(description = "所属部门")
    private Long deptId;
}
