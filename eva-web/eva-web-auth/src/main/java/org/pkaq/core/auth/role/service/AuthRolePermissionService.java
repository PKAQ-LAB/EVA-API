package org.pkaq.core.auth.role.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.role.mapper.AuthRoleResourceMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 认证角色资源服务
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class AuthRolePermissionService {

    private final AuthRoleResourceMapper roleResourceMapper;

    /**
     * 查询角色与资源路径映射
     *
     * @return 角色编码与路径集合
     */
    public List<Map<String, String>> listRoleNamesWithPath() {
        return roleResourceMapper.listRoleNamesWithPath();
    }
}
