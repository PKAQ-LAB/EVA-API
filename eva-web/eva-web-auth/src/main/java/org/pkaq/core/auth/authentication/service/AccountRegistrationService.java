package org.pkaq.core.auth.authentication.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.account.AccountCreationException;
import org.pkaq.core.auth.AuthCodes;
import org.pkaq.core.auth.authentication.bo.RegistrationBo;
import org.pkaq.core.auth.config.RegistrationPolicy;
import org.pkaq.core.auth.spi.IAccountRegistration;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 独立模式自助注册，不自动登录、不赋予管理资料或角色。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Service
@RequiredArgsConstructor
public class AccountRegistrationService {
    private final EvaConfig evaConfig;
    private final IAccountRegistration registration;

    /** 校验原始输入并返回安全字符串账号ID，业务异常不附加原始凭据。 */
    public String register(RegistrationBo request) {
        if (!RegistrationPolicy.isEnabled(evaConfig)) {
            AuthCodes.REGISTRATION_DISABLED.newException();
        }
        if (null == request || null == request.getAccount()
                || !request.getAccount().matches("[A-Za-z0-9][A-Za-z0-9_.-]{2,63}")) {
            AuthCodes.REGISTRATION_INVALID_ACCOUNT.newException();
        }
        String password = request.getPassword();
        if (null == password || password.codePointCount(0, password.length()) < 8 || password.isBlank()
                || password.getBytes(StandardCharsets.UTF_8).length > 72
                || password.chars().anyMatch(Character::isISOControl)) {
            AuthCodes.REGISTRATION_INVALID_PASSWORD.newException();
        }
        String nickName = request.getNickName();
        if (null != nickName && (nickName.length() > 64 || nickName.chars().anyMatch(Character::isISOControl))) {
            AuthCodes.REGISTRATION_INVALID_NICKNAME.newException();
        }
        // 兼容现有前端MD5登录契约，MD5不构成安全增强，持久化仍由共享入口单次BCrypt。
        String loginPassword = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        try {
            Long accountId = registration.create(request.getAccount(), loginPassword, nickName);
            if (null == accountId || accountId <= 0L) {
                throw new IllegalStateException("账号创建未返回有效编号");
            }
            return accountId.toString();
        } catch (AccountCreationException exception) {
            switch (exception.getReason()) {
                case INVALID_ACCOUNT -> AuthCodes.REGISTRATION_INVALID_ACCOUNT.newException();
                case MISSING_PASSWORD -> AuthCodes.REGISTRATION_INVALID_PASSWORD.newException();
                case DUPLICATE_ACCOUNT -> AuthCodes.REGISTRATION_DUPLICATE_ACCOUNT.newException();
            }
            throw new IllegalStateException("账号创建失败");
        }
    }
}
