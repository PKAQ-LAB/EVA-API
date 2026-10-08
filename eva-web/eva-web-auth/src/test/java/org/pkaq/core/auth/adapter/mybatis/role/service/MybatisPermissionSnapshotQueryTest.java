package org.pkaq.core.auth.adapter.mybatis.role.service;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.adapter.mybatis.role.entity.AuthRoleEntity;
import org.pkaq.core.auth.adapter.mybatis.role.mapper.AuthRoleMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证角色实体向普通权限快照的映射完整性。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
class MybatisPermissionSnapshotQueryTest {
    @Test
    void snapshotPreservesNormalizedRoleAndDataScope() {
        AuthRoleMapper mapper = mock(AuthRoleMapper.class);
        AuthRoleEntity role = new AuthRoleEntity();
        role.setId(9L);
        role.setCode("admin");
        role.setName("管理员");
        role.setDataScope("CUSTOM");
        role.setDataOrgIds("1,2");
        when(mapper.selectByUserId(7L)).thenReturn(List.of(role));

        var snapshot = new MybatisPermissionSnapshotQuery(mapper).findRoles(7L).getFirst();

        assertEquals(9L, snapshot.getId());
        assertEquals("ROLE_ADMIN", snapshot.getCode());
        assertEquals("管理员", snapshot.getName());
        assertEquals("CUSTOM", snapshot.getDataScope());
        assertEquals("1,2", snapshot.getDataOrgIds());
    }
}
