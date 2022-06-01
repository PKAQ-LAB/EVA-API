package io.nerv.web.sys.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 角色模块关系表
 * @author: S.PKAQ
 */
@Data
@Alias("roleModule")
@TableName("sys_role_module")
@Schema(title = "角色模块关系")
public class RoleModuleEntity{
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(name = "角色ID")
    private String roleId;

    @Schema(name = "模块ID")
    private String moduleId;

    @Schema(name = "资源ID")
    private String resourceId;
}
