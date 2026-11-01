package org.pkaq.core.mybatis.log;

import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import java.util.Set;
import java.util.function.Supplier;

/**
 * 登录日志专用同步窗口，历史授权与系统写入互不替代。
 * @author PKAQ
 * @date 2026-10-30
 */
public final class LoginLogQueryScope {
    private static final String MAPPER = "org.pkaq.core.auth.adapter.mybatis.audit.mapper.LoginLogMapper.";
    private static final Set<String> HISTORY = Set.of("selectHistoryDetail", "selectHistoryPage",
            "selectHistoryPage_mpCount");
    private static final Set<String> SYSTEM = Set.of("selectRiskDeviceCount", "selectRiskLastLogin", "update");
    private static final ThreadLocal<ReadScope> READ = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> STORAGE = new ThreadLocal<>();

    private LoginLogQueryScope() {
    }

    /** 在已完成资格核验后固定主体及租户条件，退出时恢复外层窗口。 */
    public static <T> T read(Long tenantId, Supplier<T> action) {
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();
        if (user == null || user.getUserId() <= 0L || user.getTenantId() < 0L
                || tenantId != null && tenantId < 0L
                || user.getTenantId() > 0L && !Long.valueOf(user.getTenantId()).equals(tenantId)) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        ReadScope previous = READ.get();
        READ.set(new ReadScope(user, user.getUserId(), user.getTenantId(), tenantId));
        try {
            return action.get();
        } finally {
            if (previous == null) {
                READ.remove();
            } else {
                READ.set(previous);
            }
        }
    }

    /** 系统写事务窗口不提供人类历史读取资格。 */
    public static void write(Runnable action) {
        Boolean previous = STORAGE.get();
        STORAGE.set(Boolean.TRUE);
        try {
            action.run();
        } finally {
            if (previous == null) {
                STORAGE.remove();
            } else {
                STORAGE.set(previous);
            }
        }
    }

    /** 对固定历史语句核验当前身份，并返回不可由查询参数放宽的租户条件。 */
    public static Long requireHistory(String statement) {
        ReadScope scope = READ.get();
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();
        if (!isHistory(statement) || scope == null || user != scope.user()
                || user.getUserId() != scope.userId() || user.getTenantId() != scope.subjectTenant()) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        return scope.tenantId();
    }

    /** 仅允许统一存储窗口内的固定风险查询及会话更新。 */
    public static void requireStorage(String statement) {
        if (!isSystem(statement) || !Boolean.TRUE.equals(STORAGE.get())) {
            CommonCodes.PERMISSION_DENY.newException();
        }
    }

    /** 判断历史专用语句，禁止按整个 Mapper 前缀豁免。 */
    public static boolean isHistory(String statement) {
        return statement != null && statement.startsWith(MAPPER)
                && HISTORY.contains(statement.substring(MAPPER.length()));
    }

    /** 判断系统固定语句，禁止将风控窗口用于历史查询。 */
    public static boolean isSystem(String statement) {
        return statement != null && statement.startsWith(MAPPER)
                && SYSTEM.contains(statement.substring(MAPPER.length()));
    }

    private record ReadScope(ThreadUser user, long userId, long subjectTenant, Long tenantId) {
    }
}
