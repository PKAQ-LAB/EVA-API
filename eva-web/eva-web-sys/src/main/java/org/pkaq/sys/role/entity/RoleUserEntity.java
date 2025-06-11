package org.pkaq.sys.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

/**
 * 角色用户关系表
 *
 * @author: S.PKAQ
 */
@Data
@Alias("roleUser")
@TableName("sys_role_user")
@EqualsAndHashCode()
public class RoleUserEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @NotBlank
    private String roleId;

    @NotBlank
    private String userId;
}
