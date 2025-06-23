package org.pkaq.sys.role.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.StdTreeVo;
import org.pkaq.sys.role.entity.RoleModuleEntity;
import org.pkaq.sys.role.entity.RoleUserEntity;

import java.util.List;
import java.util.Map;

/**
 * 角色列表vo
 * @author: S.PKAQ
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoleListVo extends StdTreeVo {

    /** 数据权限类型 **/
    private String dataPermissionType;

    /** 角色拥有的模块列表 **/
    private List<RoleModuleEntity> modules;

    /** 角色拥有的用户列表 **/
    private List<RoleUserEntity> users;

    /** 模块权限 **/
    private Map<String, String[]> resources;
}
