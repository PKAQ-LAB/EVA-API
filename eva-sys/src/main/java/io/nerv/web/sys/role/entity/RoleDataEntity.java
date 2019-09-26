package io.nerv.web.sys.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 角色数据权限
 * @author: S.PKAQ
 * @Datetime: 2018/4/13 7:26
 */
@Data
@Alias("dataperm")
@TableName("sys_role_dataperm")
@ApiModel("角色数据权限")
public class RoleDataEntity {
    @TableId(type = IdType.ID_WORKER_STR)
    private String id;

    @ApiModelProperty("角色ID")
    private String roleId;

    @ApiModelProperty("部门ID")
    private String deptId;
}
