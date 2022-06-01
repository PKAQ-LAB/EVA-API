package io.nerv.web.sys.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotBlank;

/**
 * 角色用户关系表
 * @author: S.PKAQ
 */
@Data
@Alias("roleUser")
@TableName("sys_role_user")
@EqualsAndHashCode()
@Schema(title = "角色用户关系")
public class RoleUserEntity{
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @NotBlank
    @Schema(name = "角色ID")
    private String roleId;

    @NotBlank
    @Schema(name = "用户ID")
    private String userId;
}
