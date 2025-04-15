package org.pkaq.sys.tenant.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.Vo;

/**
 * 租户管理列表视图对象
 *
 * @author 茂茂AdamEve
 */
@Schema(description = "角色/用户/部门/岗位管理左侧租户列表视图对象")
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantLeftListVo implements Vo {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @Schema(description = "租户名称")
    private String name;
    @Schema(description = "租户编码")
    private String code;
    @Schema(description = "租户类型")
    private String type;
    @Schema(description = "全称")
    private String fullName;
}
