package org.pkaq.core.mybatis.log;

import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import java.util.Set;
import java.util.function.Supplier;

/**
 * 已核验集中审计资格的同步读取窗口，仅对指定Mapper读取生效。
 * @author PKAQ
 * @date 2026-10-24
 */
public final class OpenApiAuditQueryScope {
    private static final ThreadLocal<ThreadUser> AUTHORIZED_USER = new ThreadLocal<>();
    private static final String MAPPER = "org.pkaq.core.auth.adapter.mybatis.audit.mapper.OpenApiCallLogMapper.";
    private static final Set<String> READ_METHODS = Set.of("selectById", "selectList", "selectPage", "selectCount",
            "selectList_mpCount", "selectPage_mpCount");

    private OpenApiAuditQueryScope() {
    }

    /**
     * 仅由已完成有效管理员及资源授权核验的服务调用，退出时恢复读取窗口。
     * @param action 同步读取动作
     * @param <T> 结果类型
     * @return 读取结果
     */
    public static <T> T read(Supplier<T> action) {
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();
        if (user == null || user.getUserId() <= 0L || user.getTenantId() != 0L) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        ThreadUser previous = AUTHORIZED_USER.get();
        AUTHORIZED_USER.set(user);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                AUTHORIZED_USER.remove();
            } else {
                AUTHORIZED_USER.set(previous);
            }
        }
    }

    /**
     * 对日志读取语句核验当前同步窗口及同一个可信身份，其他Mapper不予豁免。
     * @param statementId 实际Mapper语句标识
     */
    public static void requireAuthorized(String statementId) {
        ThreadUser current = ThreadUserHelper.getCurrentUserOrNull();
        if (current == null || current.getUserId() <= 0L || current.getTenantId() != 0L
                || current != AUTHORIZED_USER.get() || statementId == null || !statementId.startsWith(MAPPER)
                || !READ_METHODS.contains(statementId.substring(MAPPER.length()))) {
            CommonCodes.PERMISSION_DENY.newException();
        }
    }
}
