package org.pkaq.sys.role.vo;

import lombok.Data;
import org.pkaq.core.mvc.vo.Vo;
import org.pkaq.sys.role.entity.RoleModuleEntity;
import org.pkaq.sys.role.entity.RoleUserEntity;

import java.util.List;
import java.util.Map;

/**
 * 角色列表vo
 * @author: S.PKAQ
 */
@Data
public class RoleListVo implements Vo {

    /** 角色名称 **/
    private String name;

    /** 角色编码 **/
    private String code;

    /** 上级角色ID **/
    private String pid;

    /** 上级角色id path **/
    private String path;

    /** 是否是叶子 **/
    private Byte isleaf;

    /** 数据权限类型 **/
    private String dataPermissionType;

    /** 角色拥有的模块列表 **/
    private List<RoleModuleEntity> modules;

    /** 角色拥有的用户列表 **/
    private List<RoleUserEntity> users;

    /** 模块权限 **/
    private Map<String, String[]> resources;
}
