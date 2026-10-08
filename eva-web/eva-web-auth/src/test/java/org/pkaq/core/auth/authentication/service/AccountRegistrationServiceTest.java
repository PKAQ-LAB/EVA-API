package org.pkaq.core.auth.authentication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.pkaq.core.account.AccountCreationException;
import org.pkaq.core.auth.authentication.bo.RegistrationBo;
import org.pkaq.core.auth.config.RegistrationPolicy;
import org.pkaq.core.auth.spi.IAccountRegistration;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 注册的能力边界、密码兼容与凭据脱敏隔离测试。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
class AccountRegistrationServiceTest {
    @Test
    void registrationDefaultsClosedWithoutStorageAccess() {
        IAccountRegistration accounts = mock(IAccountRegistration.class);
        assertThrows(BizException.class, () -> new AccountRegistrationService(new EvaConfig(), accounts)
                .register(request("demo", "Password123")));
        verifyNoInteractions(accounts);
    }

    @Test
    void tenantPlatformOrDisabledAuthenticationCannotOpenRegistration() {
        EvaConfig config = enabledConfig();
        config.setMode("platform");
        assertFalse(RegistrationPolicy.isEnabled(config));
        config.setMode("standalone");
        config.getTenant().setEnable(true);
        assertFalse(RegistrationPolicy.isEnabled(config));
        config.getTenant().setEnable(false);
        config.getAuth().getAuthentication().setEnabled(false);
        assertFalse(RegistrationPolicy.isEnabled(config));
    }

    @Test
    void successfulRegistrationUsesExistingMd5LoginContractAndStringIdentifier() {
        IAccountRegistration accounts = mock(IAccountRegistration.class);
        String digest = DigestUtils.md5DigestAsHex("Password123".getBytes(StandardCharsets.UTF_8));
        when(accounts.create("demo", digest, "昵称")).thenReturn(9007199254740993L);
        assertEquals("9007199254740993", new AccountRegistrationService(enabledConfig(), accounts)
                .register(request("demo", "Password123")));
        verify(accounts).create("demo", digest, "昵称");
    }

    @Test
    void passwordValidationCountsUnicodeCharactersAndBytesAndRejectsControlCharacters() {
        IAccountRegistration accounts = mock(IAccountRegistration.class);
        AccountRegistrationService service = new AccountRegistrationService(enabledConfig(), accounts);
        for (String password : new String[]{"中文三", "1234567", "密".repeat(25), "1234567\n", "        "}) {
            BizException failure = assertThrows(BizException.class, () -> service.register(request("demo", password)));
            assertFalse(failure.getMessage().contains(password));
        }
        verifyNoInteractions(accounts);
    }

    @Test
    void invalidAccountOrNicknameNeverReachesPersistence() {
        IAccountRegistration accounts = mock(IAccountRegistration.class);
        AccountRegistrationService service = new AccountRegistrationService(enabledConfig(), accounts);
        for (String account : new String[]{"ab", "mail@example.com", " demo ", "user/name", "a".repeat(65)}) {
            assertThrows(BizException.class, () -> service.register(request(account, "Password123")));
        }
        RegistrationBo request = request("demo", "Password123");
        request.setNickName("昵称\n");
        assertThrows(BizException.class, () -> service.register(request));
        verifyNoInteractions(accounts);
    }

    @Test
    void multibytePasswordAtExactlySeventyTwoBytesIsAccepted() {
        IAccountRegistration accounts = mock(IAccountRegistration.class);
        String password = "密".repeat(24);
        String digest = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        when(accounts.create("demo", digest, "昵称")).thenReturn(7L);
        assertEquals("7", new AccountRegistrationService(enabledConfig(), accounts)
                .register(request("demo", password)));
    }

    @Test
    void missingCreatedIdentifierCannotProduceRegistrationSuccess() {
        IAccountRegistration accounts = mock(IAccountRegistration.class);
        assertThrows(IllegalStateException.class, () -> new AccountRegistrationService(enabledConfig(), accounts)
                .register(request("demo", "Password123")));
    }

    @Test
    void duplicateAccountIsStableBusinessFailureWithoutStorageDetails() {
        IAccountRegistration accounts = mock(IAccountRegistration.class);
        String digest = DigestUtils.md5DigestAsHex("Password123".getBytes(StandardCharsets.UTF_8));
        when(accounts.create("demo", digest, "昵称")).thenThrow(
                new AccountCreationException(AccountCreationException.Reason.DUPLICATE_ACCOUNT));
        BizException failure = assertThrows(BizException.class,
                () -> new AccountRegistrationService(enabledConfig(), accounts).register(request("demo", "Password123")));
        assertFalse(failure.getMessage().contains("Password123"));
        assertFalse(failure.getMessage().contains(digest));
    }

    @Test
    void passwordIsWriteOnlyAndUnknownAuthorityFieldsAreRejected() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        RegistrationBo request = mapper.readValue(
                "{\"account\":\"demo\",\"password\":\"Password123\",\"nickName\":\"昵称\"}", RegistrationBo.class);
        assertEquals("Password123", request.getPassword());
        assertFalse(mapper.writeValueAsString(request).contains("Password123"));
        assertFalse(request.toString().contains("Password123"));
        for (String field : new String[]{"id", "role", "roles", "tenantId", "deptId", "frozen", "profile"}) {
            assertThrows(Exception.class, () -> mapper.readValue(
                    "{\"account\":\"demo\",\"" + field + "\":1}", RegistrationBo.class));
        }
    }

    private EvaConfig enabledConfig() {
        EvaConfig config = new EvaConfig();
        config.getAuth().getRegistration().setEnabled(true);
        return config;
    }

    private RegistrationBo request(String account, String password) {
        RegistrationBo request = new RegistrationBo();
        request.setAccount(account);
        request.setPassword(password);
        request.setNickName("昵称");
        return request;
    }
}
