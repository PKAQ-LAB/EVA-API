package org.pkaq.sys.tenant.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.Vo;

import java.util.Date;

/**
 * 租户管理列表视图对象
 *
 * @author 茂茂AdamEve
 */
@Schema(description = "租户管理列表视图对象")
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantListVo implements Vo {
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
    @Schema(description = "证件类型")
    private String cardType;
    @Schema(description = "证件号")
    private String idCard;
    @Schema(description = "联系人")
    private String contactName;
    @Schema(description = "联系方式")
    private String contactTel;
    @Schema(description = "授权用户数")
    private int authUserCount;
    @Schema(description = "到期时间")
    private Date expirationDate;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "角色id")
    private String roleId;
    @Schema(description = "角色名称")
    private String roleName;
}
