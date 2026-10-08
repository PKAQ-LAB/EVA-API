package org.pkaq.core.mybatis.account.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.account.AccountCreationCommand;
import org.pkaq.core.account.AccountCreationException;
import org.pkaq.core.account.IAccountCreation;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mybatis.account.entity.AccountEntity;
import org.pkaq.core.mybatis.account.mapper.AccountMapper;
import org.pkaq.core.util.BCryptUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

/**
 * 统一账号校验、密码哈希和创建，不写管理档案或授权关系。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Service
@RequiredArgsConstructor
public class AccountCreationService implements IAccountCreation {
    private static final Set<String> ILLEGAL_ACCOUNTS =
            Set.of("null", "undefined", "true", "false", "admin", "root", "");
    private final AccountMapper accountMapper;

    /** 沿用管理端账号保留字校验。 */
    @Override
    public void validateAccount(String account) {
        if (account == null || ILLEGAL_ACCOUNTS.contains(account.trim().toLowerCase(Locale.ROOT))) {
            throw new AccountCreationException(AccountCreationException.Reason.INVALID_ACCOUNT);
        }
    }

    /** 在调用者已经选定的可信数据库范围内创建账号。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AccountCreationCommand command) {
        if (command == null) {
            throw new AccountCreationException(AccountCreationException.Reason.INVALID_ACCOUNT);
        }
        validateAccount(command.getAccount());
        if (command.getPassword() == null || command.getPassword().isBlank()) {
            throw new AccountCreationException(AccountCreationException.Reason.MISSING_PASSWORD);
        }
        Long existing = accountMapper.countActiveAccount(command.getAccount());
        if (existing != null && existing > 0L) {
            throw new AccountCreationException(AccountCreationException.Reason.DUPLICATE_ACCOUNT);
        }
        AccountEntity entity = new AccountEntity();
        entity.setId(command.getId() == null || command.getId() == 0L ? IdWorker.getId() : command.getId());
        entity.setAccount(command.getAccount());
        entity.setPassword(BCryptUtils.hashpw(command.getPassword()));
        entity.setAvatar(command.getAvatar());
        entity.setNickName(command.getNickName());
        entity.setTel(command.getTel());
        entity.setEmail(command.getEmail());
        entity.setLastIp(command.getLastIp());
        entity.setLastLogin(command.getLastLogin());
        entity.setFrozen(command.getFrozen() == null ? FrozenEnumm.UN_FROZEN : command.getFrozen());
        entity.setRemark(command.getRemark());
        entity.setSort(command.getSort());
        entity.setRevision(0);
        entity.setPermVer(0L);
        try {
            // 只有实际写入一条账号记录才返回主键，避免拦截或存储失败造成虚假创建成功。
            if (1 != accountMapper.insert(entity)) {
                throw new IllegalStateException("账号创建失败");
            }
        } catch (DuplicateKeyException exception) {
            // 数据库唯一索引兜住并发创建，不向调用者暴露 SQL 和明文凭据。
            throw new AccountCreationException(AccountCreationException.Reason.DUPLICATE_ACCOUNT);
        }
        return entity.getId();
    }
}
