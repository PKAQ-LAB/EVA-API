package io.nerv.web.sys.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 角色配置
 * @author: S.PKAQ
 */
@Data
@Alias("roleConfig")
@TableName("sys_role_configuration")
@ApiModel("角色参数权限")
public class RoleConfigEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty("角色ID")
    private String roleId;

    @ApiModelProperty("参数key")
    private String paramKey;

    @ApiModelProperty("参数值")
    private String paramVal;
}
