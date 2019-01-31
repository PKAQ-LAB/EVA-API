package io.nerv.web.sys.role.entity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

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
@ApiModel("角色用户关系")
public class RoleUserEntity{
    @TableId
    private String id;
    @ApiModelProperty("角色ID")
    private String roleId;
    @ApiModelProperty("用户ID")
    private String userId;
}
