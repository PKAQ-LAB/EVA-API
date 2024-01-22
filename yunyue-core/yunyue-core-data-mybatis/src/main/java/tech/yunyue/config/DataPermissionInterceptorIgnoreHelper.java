package tech.yunyue.config;

import cn.hutool.core.collection.CollUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 数据权限拦截器忽略策略管理助手类
 * @author 茂茂AdamEve
 */
public abstract class DataPermissionInterceptorIgnoreHelper {
    /**
     * 本地线程拦截器忽主查询statement
     */
    private static final ThreadLocal<Set<String>> MAIN_STRATEGY_LOCAL = ThreadLocal.withInitial(HashSet::new);

    /**
     * 手动设置当前线程的主查询statement 主查询才需要数据鉴权
     */
    public static void handle(String... mainStatementId) {
        MAIN_STRATEGY_LOCAL.get().addAll(List.of(mainStatementId));
    }

    /**
     * 清空本地忽略策略
     */
    public static void clearIgnoreStrategy() {
        MAIN_STRATEGY_LOCAL.remove();
    }

    /**
     * 检查是否应忽略给定语句 ID 的数据权限。
     *
     * @param id 语句 ID
     * @return 如果应忽略数据权限，则为 true，否则为 false。
     */
    public static boolean willIgnoreDataPermission(String id) {
        return willIgnore(id);
    }

    public static boolean willIgnore(String id) {
        var mainIds = MAIN_STRATEGY_LOCAL.get();
        // 当前线程存在主查询StatementId且当前StatementId不是主查询时，忽略数据鉴权
        if (CollUtil.isNotEmpty(mainIds) && !mainIds.contains(id)) {
            return true;
        }
        return false;
    }
}
