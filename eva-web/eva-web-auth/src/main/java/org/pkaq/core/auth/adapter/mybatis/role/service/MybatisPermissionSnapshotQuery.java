package org.pkaq.core.auth.adapter.mybatis.role.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.adapter.mybatis.role.entity.AuthRoleEntity;
import org.pkaq.core.auth.adapter.mybatis.role.mapper.AuthRoleMapper;
import org.pkaq.core.auth.spi.IPermissionSnapshotQuery;
import org.pkaq.core.auth.spi.model.RoleSnapshot;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 将持久化角色转换为权限快照的适配器。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Service
@RequiredArgsConstructor
public class MybatisPermissionSnapshotQuery implements IPermissionSnapshotQuery {
    private final AuthRoleMapper roleMapper;

    /**
     * 在已绑定的租户上下文中查询有效角色。
     * @param userId 用户编号
     * @return 纯权限快照列表
     */
    @Override
    public List<RoleSnapshot> findRoles(Long userId) {
        List<AuthRoleEntity> roles = roleMapper.selectByUserId(userId);
        return null == roles ? List.of() : roles.stream().map(this::toSnapshot).toList();
    }

    private RoleSnapshot toSnapshot(AuthRoleEntity role) {
        RoleSnapshot snapshot = new RoleSnapshot();
        snapshot.setId(role.getId());
        snapshot.setCode(role.getCode());
        snapshot.setName(role.getName());
        snapshot.setDataScope(role.getDataScope());
        snapshot.setDataOrgIds(role.getDataOrgIds());
        return snapshot;
    }
}
