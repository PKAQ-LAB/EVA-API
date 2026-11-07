package org.pkaq.core.mybatis.log;

import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

/**
 * 日志读取的身份与数据范围守卫。
 *
 * @author PKAQ
 * @date 2026-10-23
 */
public final class LogQueryAccess {
    private LogQueryAccess() {
    }

    /**
     * 保留原调用方式，默认检查全部数据范围。
     */
    public static void requireAll() {
        requireAll(true);
    }

    /**
     * 关闭数据权限时只检查身份，租户范围仍由查询条件限制。
     */
    public static void requireAll(boolean dataPermissionEnabled) {
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();

        // 无论权限开关如何，都必须有已认证的用户和租户
        if (user == null || user.getUserId() <= 0L || user.getTenantId() < 0L) {
            CommonCodes.PERMISSION_DENY.newException();
        }

        // 开启数据权限后，才要求具备全部数据范围
        if (dataPermissionEnabled && (user.getDataScopes() == null || user.getDataScopes().stream()
                .noneMatch(scope -> scope != null && "0000".equals(scope.getCode())))) {
            CommonCodes.PERMISSION_DENY.newException();
        }
    }
}
