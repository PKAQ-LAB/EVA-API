package org.pkaq.core.auth.adapter.mybatis.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pkaq.core.account.AccountCreationCommand;
import org.pkaq.core.account.IAccountCreation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * 注册适配器仅向共享入口传入独立账号字段。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
class MybatisAccountRegistrationTest {
    @Test
    void adapterDoesNotAssignIdentifierOrManagementFields() {
        IAccountCreation sharedCreation = mock(IAccountCreation.class);
        new MybatisAccountRegistration(sharedCreation).create("demo", "md5-password", "昵称");
        ArgumentCaptor<AccountCreationCommand> command = ArgumentCaptor.forClass(AccountCreationCommand.class);
        verify(sharedCreation).create(command.capture());
        assertEquals("demo", command.getValue().getAccount());
        assertEquals("md5-password", command.getValue().getPassword());
        assertEquals("昵称", command.getValue().getNickName());
        assertNull(command.getValue().getId());
        assertNull(command.getValue().getFrozen());
        assertNull(command.getValue().getTel());
        assertNull(command.getValue().getEmail());
    }
}
