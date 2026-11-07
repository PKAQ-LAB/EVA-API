package org.pkaq.core.mybatis.log;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 日志查询尊重数据权限开关。
 */
class LogQueryAccessTest {
    @Test
    void shouldAllowEmptyScopesWhenDataPermissionDisabled() {
        var user = new ThreadUser().setUserId(1L).setTenantId(0L).setDataScopes(List.of());

        ThreadUserHelper.runWithUser(user, () -> assertDoesNotThrow(() -> LogQueryAccess.requireAll(false)));
    }

    @Test
    void shouldRejectEmptyScopesWhenDataPermissionEnabled() {
        var user = new ThreadUser().setUserId(1L).setTenantId(0L).setDataScopes(List.of());

        ThreadUserHelper.runWithUser(user, () -> assertThrows(RuntimeException.class, () -> LogQueryAccess.requireAll(true)));
    }

    @Test
    void shouldRejectMissingIdentityEvenWhenDataPermissionDisabled() {
        assertThrows(RuntimeException.class, () -> LogQueryAccess.requireAll(false));
    }

    @Test
    void shouldPreserveEnabledAllScopeAccess() {
        var user = new ThreadUser().setUserId(1L).setTenantId(0L)
                .setDataScopes(List.of(new ThreadUser.DataScope("0000", List.of())));

        ThreadUserHelper.runWithUser(user, () -> assertDoesNotThrow(() -> LogQueryAccess.requireAll()));
    }
}
