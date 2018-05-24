package org.pkaq.sys.role.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * 角色模块关系表
 * @author: S.PKAQ
 * @Datetime: 2018/4/16 22:04
 */
@Data
@Alias("roleModule")
@TableName("sys_role_module")
@EqualsAndHashCode()
@ApiModel("角色模块关系")
public class RoleModuleEntity implements Serializable{
    @TableId
    private String id;
    @ApiModelProperty("角色ID")
    private String roleId;
    @ApiModelProperty("模块ID")
    private String moduleId;
}
