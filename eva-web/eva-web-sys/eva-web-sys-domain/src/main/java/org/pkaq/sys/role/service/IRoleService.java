package org.pkaq.sys.role.service;

import org.pkaq.core.mvc.bo.IdCodeBo;
import org.pkaq.sys.role.bo.RoleResourceRefBo;
import org.pkaq.sys.role.bo.RoleUserRefBo;
import org.pkaq.sys.role.vo.RoleGrantedModuleVo;
import org.pkaq.sys.role.vo.RoleGrantedUserVo;

import java.util.Set;

public interface IRoleService {

    void delete(Set<Long> ids);

    boolean isUnique(IdCodeBo idCodeBo);

    RoleGrantedModuleVo fetchResource(RoleResourceRefBo roleModule);

    void grantResource(RoleResourceRefBo role);

    RoleGrantedUserVo listUser(Long roleId, Long deptId);

    void grantUser(RoleUserRefBo role);
}
