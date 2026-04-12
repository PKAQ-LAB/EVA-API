package org.pkaq.core.auth.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 角色资源关系（扁平化存储，用于RBAC权限校验）
 *
 * @author PKAQ
 */
@Data
@Alias("sysRoleResource")
@TableName("SYS_ROLE_RESOURCE")
public class SysRoleResource {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 资源路径
     */
    private String resourcePath;

    /**
     * HTTP方法 (GET/POST/PUT/DELETE/*)
     */
    private String httpMethod;
}
