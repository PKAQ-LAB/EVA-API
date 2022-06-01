package io.nerv.web.sys.role.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.nerv.core.mvc.entity.mybatis.StdBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

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
@Schema(title = "角色管理")
public class RoleEntity extends StdBaseEntity {
    @TableField(condition = SqlCondition.LIKE)
    @Schema(name = "角色名称")
    private String name;

    @TableField(condition = SqlCondition.LIKE)
    @Schema(name = "角色编码")
    private String code;

    @Schema(name = "上级角色")
    private String parentId;

    @Schema(name = "上级角色名称")
    private String parentName;

    @Schema(name = "上级角色id path")
    private String path;

    @Schema(name = "角色路径名称")
    private String pathName;

    @Schema(name = "是否是叶子")
    private Byte isleaf;

    @Schema(name = "排序")
    private Integer orders;

    @Schema(name = "是否锁定")
    private String locked;

    @Schema(name = "数据权限类型")
    private String dataPermissionType;

    @Schema(name = "数据权限部门ID")
    private String dataPermissionDeptid;

    @TableField(exist = false)
    @Schema(name = "角色拥有的模块列表")
    private List<RoleModuleEntity> modules;

    @TableField(exist = false)
    @Schema(name = "角色拥有的用户列表")
    private List<RoleUserEntity> users;

    @TableField(exist = false)
    @Schema(name = "模块权限")
    private Map<String, String[]> resources;
}
