package io.nerv.web.sys.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 角色配置
 * @author: S.PKAQ
 */
@Data
@Alias("roleConfig")
@TableName("sys_role_configuration")
@Schema(title = "角色参数权限")
public class RoleConfigEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "角色ID")
    private String roleId;

    @Schema(description = "参数key")
    private String paramKey;

    @Schema(description = "参数值")
    private String paramVal;
}
