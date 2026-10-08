package org.pkaq.sys.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pkaq.core.account.AccountCreationCommand;
import org.pkaq.core.account.IAccountCreation;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.sys.post.service.UserPostRefSerivce;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.user.bo.UserAoeBo;
import org.pkaq.sys.user.convert.UserConvert;
import org.pkaq.sys.user.entity.AccountProfileEntity;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.AccountProfileMapper;
import org.pkaq.sys.user.mapper.UserMapper;
import org.pkaq.sys.user.vo.UserDetailVo;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 管理账号与可选档案联动测试，不连接数据库。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@ExtendWith(MockitoExtension.class)
class UserAccountProfileServiceTest {
    @Mock private UserMapper mapper;
    @Mock private AccountProfileMapper accountProfileMapper;
    @Mock private IAccountCreation accountCreation;
    @Mock private UserConvert convert;
    @Mock private EvaConfig evaConfig;
    @Mock private RoleUserMapper roleUserMapper;
    @Mock private UserPostRefSerivce userPostRefSerivce;
    @InjectMocks private UserService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "mapper", mapper);
    }

    /** 管理新增委托共享原语创建账号，然后创建同主键档案。 */
    @Test
    void managementCreationUsesSharedAccountCreatorAndProfile() {
        UserAoeBo bo = input();
        UserEntity entity = projected(bo);
        when(convert.boToEntity(bo)).thenReturn(entity);
        when(accountCreation.create(any(AccountCreationCommand.class))).thenReturn(101L);
        when(accountProfileMapper.insert(any(AccountProfileEntity.class))).thenReturn(1);

        service.saveUser(bo);

        ArgumentCaptor<AccountCreationCommand> account = ArgumentCaptor.forClass(AccountCreationCommand.class);
        verify(accountCreation).create(account.capture());
        assertEquals("plain-test-password", account.getValue().getPassword());
        assertEquals("ordinary-account", account.getValue().getAccount());
        ArgumentCaptor<AccountProfileEntity> profile = ArgumentCaptor.forClass(AccountProfileEntity.class);
        verify(accountProfileMapper).insert(profile.capture());
        assertEquals(101L, profile.getValue().getAccountId());
        assertEquals("worker-1", profile.getValue().getCode());
        assertEquals("测试姓名", profile.getValue().getName());
        assertNull(profile.getValue().getDeptId());
        assertEquals(101L, bo.getId());
        verify(mapper, never()).insert(any(UserEntity.class));
    }

    /** 自助注册账号没有档案时，管理员编辑可以补建且不重复创建账号。 */
    @Test
    void editingRegisteredAccountCreatesMissingProfile() {
        UserAoeBo bo = input();
        bo.setId(101L);
        bo.setPassword(null);
        UserEntity existing = new UserEntity();
        existing.setId(101L);
        existing.setFrozen(FrozenEnumm.UN_FROZEN);
        when(mapper.selectById(101L)).thenReturn(existing);
        when(convert.boToEntity(bo)).thenReturn(projected(bo));
        when(mapper.updateById(any(UserEntity.class))).thenReturn(1);
        when(accountProfileMapper.insert(any(AccountProfileEntity.class))).thenReturn(1);

        service.saveUser(bo);

        verify(accountCreation, never()).create(any(AccountCreationCommand.class));
        verify(mapper).updateById(any(UserEntity.class));
        verify(mapper).incrementPermVer(101L);
        verify(accountProfileMapper).insert(any(AccountProfileEntity.class));
    }

    /** 已有档案允许清空可选部门，更新不写账号凭据。 */
    @Test
    void editingProfileCanClearOptionalDepartment() {
        UserAoeBo bo = input();
        bo.setId(101L);
        bo.setPassword(null);
        UserEntity existing = new UserEntity();
        existing.setId(101L);
        existing.setFrozen(FrozenEnumm.UN_FROZEN);
        AccountProfileEntity existingProfile = new AccountProfileEntity();
        existingProfile.setAccountId(101L);
        existingProfile.setDeptId(7L);
        when(mapper.selectById(101L)).thenReturn(existing);
        when(convert.boToEntity(bo)).thenReturn(projected(bo));
        when(accountProfileMapper.selectById(101L)).thenReturn(existingProfile);
        when(mapper.updateById(any(UserEntity.class))).thenReturn(1);
        when(accountProfileMapper.updateById(any(AccountProfileEntity.class))).thenReturn(1);

        service.saveUser(bo);

        ArgumentCaptor<AccountProfileEntity> profile = ArgumentCaptor.forClass(AccountProfileEntity.class);
        verify(accountProfileMapper).updateById(profile.capture());
        assertEquals(101L, profile.getValue().getAccountId());
        assertNull(profile.getValue().getDeptId());
        verify(accountProfileMapper, never()).insert(any(AccountProfileEntity.class));
        verify(accountCreation, never()).create(any(AccountCreationCommand.class));
    }

    /** 账号乐观锁失败时，禁止档案与权限版本独立更新。 */
    @Test
    void accountUpdateConflictDoesNotWriteProfile() {
        UserAoeBo bo = input();
        bo.setId(101L);
        bo.setPassword(null);
        UserEntity existing = new UserEntity();
        existing.setId(101L);
        existing.setFrozen(FrozenEnumm.UN_FROZEN);
        when(mapper.selectById(101L)).thenReturn(existing);
        when(convert.boToEntity(bo)).thenReturn(projected(bo));

        assertThrows(IllegalStateException.class, () -> service.saveUser(bo));

        verify(accountProfileMapper, never()).selectById(101L);
        verify(accountProfileMapper, never()).insert(any(AccountProfileEntity.class));
        verify(accountProfileMapper, never()).updateById(any(AccountProfileEntity.class));
        verify(mapper, never()).incrementPermVer(101L);
    }

    /** 资料写入失败不得作为成功返回，交由事务回滚账号修改。 */
    @Test
    void profileWriteFailureRejectsWholeOperation() {
        UserAoeBo bo = input();
        when(convert.boToEntity(bo)).thenReturn(projected(bo));
        when(accountCreation.create(any(AccountCreationCommand.class))).thenReturn(101L);

        assertThrows(IllegalStateException.class, () -> service.saveUser(bo));
    }

    /** 旧兼容入口不再接受 standalone 或字段级租户模式。 */
    @Test
    void legacyTenantAdminEntryRejectsStandalone() {
        assertThrows(IllegalStateException.class, () -> service.createTenantAdmin(null));
        verify(accountCreation, never()).create(any(AccountCreationCommand.class));
    }

    /** 详情保留无档案账号及其可选部门、岗位语义。 */
    @Test
    void detailsKeepAccountWithoutProfile() {
        UserEntity account = new UserEntity();
        account.setId(101L);
        account.setAccount("ordinary-account");
        UserDetailVo detail = new UserDetailVo();
        detail.setId(101L);
        detail.setAccount("ordinary-account");
        when(mapper.selectById(101L)).thenReturn(account);
        when(convert.entityToDetailVo(account)).thenReturn(detail);
        when(roleUserMapper.selectRoleIds(101L)).thenReturn(List.of());
        when(userPostRefSerivce.listPostIdsByUserId(101L)).thenReturn(List.of());

        assertEquals(detail, service.getUser(101L));
        assertNull(detail.getDeptId());
        assertNull(detail.getCode());
    }

    /** 批量操作仍拒绝受保护账号，不受档案字段移出账号表影响。 */
    @Test
    void protectsReservedCodeOnBatchMutation() {
        when(mapper.countReadOnlyAccounts(Set.of(101L))).thenReturn(1L);

        assertThrows(RuntimeException.class, () -> service.updateUser(Set.of(101L)));
        verify(mapper, never()).change(any());
        verify(accountProfileMapper, never()).insert(any(AccountProfileEntity.class));
    }

    private UserAoeBo input() {
        UserAoeBo bo = new UserAoeBo();
        bo.setAccount("ordinary-account");
        bo.setPassword("plain-test-password");
        bo.setCode("worker-1");
        bo.setName("测试姓名");
        return bo;
    }

    private UserEntity projected(UserAoeBo bo) {
        UserEntity entity = new UserEntity();
        entity.setId(bo.getId());
        entity.setAccount(bo.getAccount());
        entity.setCode(bo.getCode());
        entity.setName(bo.getName());
        return entity;
    }
}
