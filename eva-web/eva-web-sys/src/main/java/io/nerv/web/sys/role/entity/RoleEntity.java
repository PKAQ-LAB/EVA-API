package io.nerv.web.sys.role.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import io.nerv.core.mvc.entity.mybatis.StdBaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 角色管理模型类
 * @author: S.PKAQ
 */
@Data
@Alias("role")
@TableName("sys_role")
@EqualsAndHashCode(callSuper = true)
@ApiModel("角色管理")
public class RoleEntity extends StdBaseEntity {
    @TableField(condition = SqlCondition.LIKE)
    @ApiModelProperty("角色名称")
    private String name;

    @TableField(condition = SqlCondition.LIKE)
    @ApiModelProperty("角色编码")
    private String code;

    @ApiModelProperty("上级角色")
    private String parentId;

    @ApiModelProperty("上级角色名称")
    private String parentName;

    @ApiModelProperty("上级角色id path")
    private String path;

    @ApiModelProperty("角色路径名称")
    private String pathName;

    @ApiModelProperty("是否是叶子")
    private Byte isleaf;

    @ApiModelProperty("排序")
    private Integer orders;

    @ApiModelProperty("是否锁定")
    private String locked;

    @ApiModelProperty("数据权限类型")
    private String dataPermissionType;

    @ApiModelProperty("数据权限部门ID")
    private String dataPermissionDeptid;

    @TableField(exist = false)
    @ApiModelProperty("角色拥有的模块列表")
    private List<RoleModuleEntity> modules;

    @TableField(exist = false)
    @ApiModelProperty("角色拥有的用户列表")
    private List<RoleUserEntity> users;

    @TableField(exist = false)
    @ApiModelProperty("模块权限")
    private Map<String, String[]> resources;
}
