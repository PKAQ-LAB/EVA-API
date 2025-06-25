package org.pkaq.sys.role.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.StdTreeBo;
import org.pkaq.sys.role.entity.RoleModuleEntity;
import org.pkaq.sys.role.entity.RoleUserEntity;

import java.util.List;
import java.util.Map;

/**
 * @author PKAQ
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "角色管理AOE bo")
public class RoleAoeBo extends StdTreeBo {

    @Schema(description = "数据权限类型")
    private String dataPermissionType;

    @Schema(description = "角色拥有的模块列表")
    private List<RoleModuleEntity> modules;

    @Schema(description = "角色拥有的用户列表")
    private List<RoleUserEntity> users;

    @Schema(description = "模块权限")
    private Map<String, String[]> resources;
}
