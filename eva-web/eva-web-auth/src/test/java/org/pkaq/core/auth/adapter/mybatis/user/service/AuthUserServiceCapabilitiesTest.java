package org.pkaq.core.auth.adapter.mybatis.user.service;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.adapter.mybatis.user.entity.AuthUserEntity;
import org.pkaq.core.auth.adapter.mybatis.user.mapper.AuthUserMapper;
import org.pkaq.core.auth.spi.model.AccountSnapshot;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.util.json.JsonUtil;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 账号适配器的只读映射与权限存储隔离测试。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
class AuthUserServiceCapabilitiesTest {
    @Test
    void accountSnapshotDoesNotExposePasswordInJsonOrDiagnosticText() {
        AccountSnapshot snapshot = new AccountSnapshot();
        snapshot.setPassword("sensitive-hash");
        assertFalse(JsonUtil.toJson(snapshot).contains("sensitive-hash"));
        assertFalse(snapshot.toString().contains("sensitive-hash"));
    }

    @Test
    void accountLookupPreservesSecurityFieldsWithoutJoinedRoles() {
        EvaConfig config = new EvaConfig();
        AuthUserMapper mapper = mock(AuthUserMapper.class);
        AuthUserEntity entity = new AuthUserEntity();
        entity.setId(7L);
        entity.setAccount("demo");
        entity.setPassword("hash");
        entity.setName("用户");
        entity.setNickName("昵称");
        entity.setDeptId(9L);
        entity.setPermVer(3L);
        entity.setFrozen(FrozenEnumm.FROZEN);
        entity.setTenantId(11L);
        entity.setTenantFrozen(FrozenEnumm.UN_FROZEN);
        Date expiration = new Date(123456L);
        entity.setTenantExpirationDate(expiration);
        when(mapper.getUserAccount(any())).thenReturn(entity);
        when(mapper.getTenantUserAccount(any())).thenReturn(entity);
        when(mapper.getAuthState(7L)).thenReturn(entity);
        AuthUserService service = new AuthUserService(mapper, config);

        AccountSnapshot snapshot = service.getAccount("demo");
        assertEquals(7L, snapshot.getId());
        assertEquals("demo", snapshot.getAccount());
        assertEquals("hash", snapshot.getPassword());
        assertEquals("用户", snapshot.getName());
        assertEquals("昵称", snapshot.getNickName());
        assertEquals(9L, snapshot.getDeptId());
        assertEquals(3L, snapshot.getPermVer());
        assertEquals(FrozenEnumm.FROZEN, snapshot.getFrozen());
        assertEquals(11L, snapshot.getTenantId());
        assertEquals(FrozenEnumm.UN_FROZEN, snapshot.getTenantFrozen());
        assertEquals(expiration, snapshot.getTenantExpirationDate());
        assertEquals(snapshot, service.getTenantAccount("demo"));
        assertEquals(snapshot, service.getAccountState(7L));
        verify(mapper, never()).getUserWithRole(any());
        verify(mapper, never()).getTenantUserWithRole(any());
    }

    @Test
    void accountAdapterDoesNotJoinRolesEvenWhenDataPermissionIsEnabled() {
        EvaConfig config = new EvaConfig();
        config.getDataPermission().setEnable(true);
        AuthUserMapper mapper = mock(AuthUserMapper.class);
        when(mapper.getAuthState(7L)).thenReturn(new AuthUserEntity());

        new AuthUserService(mapper, config).getAccountState(7L);

        verify(mapper).getAuthState(7L);
        verify(mapper, never()).getUserWithRole(any());
    }

    @Test
    void missingAccountAndInvalidIdentifierReturnNoSnapshot() {
        AuthUserService service = new AuthUserService(mock(AuthUserMapper.class), new EvaConfig());
        assertNull(service.getAccount("missing"));
        assertNull(service.getAccountState(null));
        assertNull(service.getAccountState(-1L));
    }
}
