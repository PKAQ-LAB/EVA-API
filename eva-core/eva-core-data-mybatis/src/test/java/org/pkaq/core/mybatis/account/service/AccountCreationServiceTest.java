package org.pkaq.core.mybatis.account.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pkaq.core.account.AccountCreationCommand;
import org.pkaq.core.account.AccountCreationException;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mybatis.account.entity.AccountEntity;
import org.pkaq.core.mybatis.account.mapper.AccountMapper;
import org.pkaq.core.util.BCryptUtils;
import org.pkaq.core.util.json.JsonUtil;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 管理新增与注册共用账号创建规则的隔离测试。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
class AccountCreationServiceTest {

    /** 创建仅写账号并对密码做一次哈希。 */
    @Test
    void createsOnlyAccountWithHashedPassword() {
        AccountMapper mapper = mock(AccountMapper.class);
        AccountCreationCommand command = command();
        command.setId(45L);
        command.setNickName("测试账号");
        when(mapper.insert(any(AccountEntity.class))).thenReturn(1);
        Long accountId = new AccountCreationService(mapper).create(command);

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(mapper).insert(captor.capture());
        AccountEntity entity = captor.getValue();
        assertEquals(45L, accountId);
        assertEquals("ordinary-account", entity.getAccount());
        assertEquals("测试账号", entity.getNickName());
        assertEquals(FrozenEnumm.UN_FROZEN, entity.getFrozen());
        assertEquals(0L, entity.getPermVer());
        assertTrue(BCryptUtils.checkpw(command.getPassword(), entity.getPassword()));
        assertFalse(command.getPassword().equals(entity.getPassword()));
    }

    /** 存储未写入账号时禁止返回生成主键，诊断不包含密码或账号。 */
    @Test
    void rejectsInsertWithoutExactlyOneStoredRow() {
        AccountMapper mapper = mock(AccountMapper.class);
        AccountCreationService service = new AccountCreationService(mapper);
        when(mapper.insert(any(AccountEntity.class))).thenReturn(0, 2);

        IllegalStateException missing = assertThrows(IllegalStateException.class, () -> service.create(command()));
        assertEquals("账号创建失败", missing.getMessage());
        assertFalse(missing.getMessage().contains("ordinary-account"));
        assertFalse(missing.getMessage().contains("plain-test-password"));
        assertThrows(IllegalStateException.class, () -> service.create(command()));
    }

    /** 非法账号和空密码在存储前拒绝。 */
    @Test
    void rejectsReservedAccountAndEmptyPassword() {
        AccountMapper mapper = mock(AccountMapper.class);
        AccountCreationService service = new AccountCreationService(mapper);
        AccountCreationCommand command = command();
        command.setAccount(" ADMIN ");
        assertEquals(AccountCreationException.Reason.INVALID_ACCOUNT,
                assertThrows(AccountCreationException.class, () -> service.create(command)).getReason());
        command.setAccount("ordinary-account");
        command.setPassword(" ");
        assertEquals(AccountCreationException.Reason.MISSING_PASSWORD,
                assertThrows(AccountCreationException.class, () -> service.create(command)).getReason());
        verify(mapper, never()).insert(any(AccountEntity.class));
    }

    /** 查询重复与数据库并发唯一冲突返回一致业务原因。 */
    @Test
    void rejectsDuplicateBeforeInsertAndOnDatabaseRace() {
        AccountMapper mapper = mock(AccountMapper.class);
        AccountCreationService service = new AccountCreationService(mapper);
        when(mapper.countActiveAccount("ordinary-account")).thenReturn(1L);
        assertEquals(AccountCreationException.Reason.DUPLICATE_ACCOUNT,
                assertThrows(AccountCreationException.class, () -> service.create(command())).getReason());
        verify(mapper, never()).insert(any(AccountEntity.class));
        when(mapper.countActiveAccount("ordinary-account")).thenReturn(0L);
        when(mapper.insert(any(AccountEntity.class))).thenThrow(new DuplicateKeyException("unique constraint"));
        assertEquals(AccountCreationException.Reason.DUPLICATE_ACCOUNT,
                assertThrows(AccountCreationException.class, () -> service.create(command())).getReason());
    }

    /** 创建参数中的明文密码不进入序列化或日志字符串。 */
    @Test
    void excludesPasswordFromSerializationAndLogString() {
        AccountCreationCommand command = command();
        assertFalse(JsonUtil.toJson(command).contains("plain-test-password"));
        assertFalse(command.toString().contains("plain-test-password"));
    }

    private AccountCreationCommand command() {
        AccountCreationCommand command = new AccountCreationCommand();
        command.setAccount("ordinary-account");
        command.setPassword("plain-test-password");
        return command;
    }
}
