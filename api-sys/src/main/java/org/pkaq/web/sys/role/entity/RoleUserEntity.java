package org.pkaq.web.sys.role.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.BaseEntity;

import java.io.Serializable;

/**
 * 角色用户关系表
 * @author: S.PKAQ
 * @Datetime: 2018/4/16 22:04
 */
@Data
@Alias("roleUser")
@TableName("sys_role_user")
@EqualsAndHashCode()
public class RoleUserEntity implements Serializable{
    @TableId
    private String id;
    /** 角色ID **/
    private String roleId;
    /** 用户ID **/
    private String userId;
}
