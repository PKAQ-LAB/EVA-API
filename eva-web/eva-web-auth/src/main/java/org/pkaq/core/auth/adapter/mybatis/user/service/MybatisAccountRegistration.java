package org.pkaq.core.auth.adapter.mybatis.user.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.account.AccountCreationCommand;
import org.pkaq.core.account.IAccountCreation;
import org.pkaq.core.auth.spi.IAccountRegistration;
import org.springframework.stereotype.Service;

/**
 * 自助注册复用共享账号创建规则，不依赖系统管理服务。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Service
@RequiredArgsConstructor
public class MybatisAccountRegistration implements IAccountRegistration {
    private final IAccountCreation accountCreation;

    /** 只转发账号、登录密码表示和昵称，不写管理资料或角色。 */
    @Override
    public Long create(String account, String loginPassword, String nickName) {
        AccountCreationCommand command = new AccountCreationCommand();
        command.setAccount(account);
        command.setPassword(loginPassword);
        command.setNickName(nickName);
        return accountCreation.create(command);
    }
}
