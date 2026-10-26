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

    /** 必须有可信身份和明确的全部数据范围；资源权限仍由请求鉴权入口检查。 */
    public static void requireAll() {
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();
        if (user == null || user.getUserId() <= 0L || user.getTenantId() < 0L
                || user.getDataScopes() == null || user.getDataScopes().stream()
                .noneMatch(scope -> scope != null && "0000".equals(scope.getCode()))) {
            CommonCodes.PERMISSION_DENY.newException();
        }
    }
}
