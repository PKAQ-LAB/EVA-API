package org.pkaq.sys.role.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;
import org.pkaq.core.mybatis.mvc.entity.StdTreeEntity;

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
public class RoleEntity extends StdTreeEntity {
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
