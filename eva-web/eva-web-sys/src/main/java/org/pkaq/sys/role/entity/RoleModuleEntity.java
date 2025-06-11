package org.pkaq.sys.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 角色模块关系表
 *
 * @author: S.PKAQ
 */
@Data
@Alias("roleModule")
@TableName("sys_role_module")
public class RoleModuleEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String roleId;

    private String moduleId;

    private String resourceId;
}
