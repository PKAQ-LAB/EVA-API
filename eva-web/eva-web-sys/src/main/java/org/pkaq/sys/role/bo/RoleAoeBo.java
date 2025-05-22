package org.pkaq.sys.role.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.bo.Bo;
import org.pkaq.sys.role.entity.RoleModuleEntity;
import org.pkaq.sys.role.entity.RoleUserEntity;

import java.util.List;
import java.util.Map;

@Data
@Schema(title = "角色管理AOE bo")
public class RoleAoeBo implements Bo {
    private String id;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "角色编码")
    private String code;

    @Schema(description = "上级角色ID")
    private String pid;

    @Schema(description = "上级角色id path")
    private String path;

    @Schema(description = "是否是叶子")
    private Byte isleaf;

    @Schema(description = "数据权限类型")
    private String dataPermissionType;

    @Schema(description = "角色拥有的模块列表")
    private List<RoleModuleEntity> modules;

    @Schema(description = "角色拥有的用户列表")
    private List<RoleUserEntity> users;

    @Schema(description = "模块权限")
    private Map<String, String[]> resources;
}
