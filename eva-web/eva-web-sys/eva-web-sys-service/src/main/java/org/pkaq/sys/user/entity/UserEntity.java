package org.pkaq.sys.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.account.entity.AccountEntity;
import org.pkaq.sys.role.entity.RoleEntity;

import java.util.List;

/**
 * 管理端账号与可选档案的联合投影，档案字段不写入账号表。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Data
@Alias("user")
@TableName(value = "SYS_ACCOUNT", excludeProperty = "tenantId")
@EqualsAndHashCode(callSuper = true)
public class UserEntity extends AccountEntity {
    @TableField(exist = false)
    private String code;
    @TableField(exist = false)
    private String name;
    @TableField(exist = false)
    private Long deptId;
    @TableField(exist = false)
    private List<RoleEntity> roles;
}
