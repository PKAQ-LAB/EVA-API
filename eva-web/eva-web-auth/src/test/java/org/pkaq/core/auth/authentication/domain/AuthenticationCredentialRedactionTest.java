package org.pkaq.core.auth.authentication.domain;

import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.adapter.mybatis.user.entity.AuthUserEntity;
import org.pkaq.core.util.json.JsonUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 内部认证投影和登录详情均不得在JSON及诊断文本中泄露密码哈希。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
class AuthenticationCredentialRedactionTest {
    private static final String TEST_CREDENTIAL = "fixed-test-password-hash";

    @Test
    void internalAccountEntityHidesCredentialButPreservesAuthenticationAccess() {
        AuthUserEntity entity = new AuthUserEntity();
        entity.setAccount("demo");
        entity.setPassword(TEST_CREDENTIAL);
        assertEquals(TEST_CREDENTIAL, entity.getPassword());
        assertFalse(entity.toString().contains(TEST_CREDENTIAL));
        assertFalse(JsonUtil.toJson(entity).contains(TEST_CREDENTIAL));
    }

    @Test
    void jwtUserDetailHidesCredentialButPreservesAuthenticationAccess() {
        JwtUserDetail detail = new JwtUserDetail(7L, "demo", 0L, TEST_CREDENTIAL,
                null, "昵称", "昵称", false, List.of());
        assertEquals(TEST_CREDENTIAL, detail.getPassword());
        assertFalse(detail.toString().contains(TEST_CREDENTIAL));
        assertFalse(JsonUtil.toJson(detail).contains(TEST_CREDENTIAL));
    }
}
