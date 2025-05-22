package org.pkaq.sys.role.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

import java.util.List;
import java.util.Map;

/**
 * 角色管理模型类
 *
 * @author: S.PKAQ
 */
@Data
@Alias("role")
@TableName("sys_role")
@EqualsAndHashCode(callSuper = true)
public class RoleEntity extends StdEntity {

    /**
     * 角色名称
     **/
    @TableField(condition = SqlCondition.LIKE)
    private String name;

    /**
     * 角色编码
     **/
    @TableField(condition = SqlCondition.LIKE)
    private String code;

    /**
     * 上级角色ID
     **/
    private String pid;

    /**
     * 上级角色id path
     **/
    private String path;

    /**
     * 是否是叶子
     **/
    private Byte isleaf;

    /**
     * 数据权限类型
     **/
    private String dataPermissionType;

    /**
     * 角色拥有的模块列表
     **/
    @TableField(exist = false)
    private List<RoleModuleEntity> modules;

    /**
     * 角色拥有的用户列表
     **/

    @TableField(exist = false)
    private List<RoleUserEntity> users;

    /**
     * 模块权限
     **/

    @TableField(exist = false)
    private Map<String, String[]> resources;
}
