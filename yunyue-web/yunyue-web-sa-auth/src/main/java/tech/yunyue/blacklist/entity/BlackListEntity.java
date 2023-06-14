package tech.yunyue.blacklist.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import tech.yunyue.core.mybatis.mvc.entity.mybatis.StdEntity;

/**
 * 黑名单管理实体类
 */
@Data
@Alias("blackList")
@TableName("sys_black_list")
@EqualsAndHashCode(callSuper = true)
@Schema(title = "黑名单管理")
public class BlackListEntity extends StdEntity {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "封禁目标不允许为空")
    @Schema(description = "封禁目标")
    private String target;

    @NotBlank(message = "封禁类型不允许为空")
    @Schema(description = "封禁类型（0000-IP、0001-请求来源URL）")
    private String category;

    //覆盖父类逻辑删除字段 不关联业务 不需要逻辑删除
    @TableField(exist = false)
    private String deleted;

    @TableField(exist = false)
    private String tenantId;

    @TableField(exist = false)
    private String companyTenantId;

}
