package org.pkaq.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户下线领域事件
 * <p>
 * 业务侧（用户/租户冻结、删除、改密 等）发布此事件，
 * 由认证侧订阅做"清 token / 强制下线"等动作，避免业务模块直接依赖 web-auth。
 * <p>
 * 使用 {@link org.springframework.transaction.event.TransactionalEventListener}
 * + AFTER_COMMIT 订阅，可保证事务回滚时不会误踢用户。
 *
 * @author PKAQ
 */
@Getter
public class UserOfflineEvent extends ApplicationEvent {
    /** 需要下线的用户 ID 集合 */
    private final Set<Long> uids;
    /** 触发原因（仅用于日志/审计；监听方的清 token 行为对所有 reason 一致） */
    private final OfflineReason reason;

    public UserOfflineEvent(Object source, Collection<Long> uids, OfflineReason reason) {
        super(source);
        this.uids = (uids == null || uids.isEmpty()) ? Set.of() : new HashSet<>(uids);
        this.reason = reason;
    }

    public UserOfflineEvent(Object source, Long uid, OfflineReason reason) {
        super(source);
        this.uids = uid == null ? Set.of() : Set.of(uid);
        this.reason = reason;
    }

    /**
     * 下线触发场景，订阅方可按 reason 决定是否过滤
     */
    public enum OfflineReason {
        /** 用户被管理员冻结 */
        USER_FROZEN,
        /** 用户被删除 */
        USER_DELETED,
        /** 用户改密（含本人和管理员重置） */
        PASSWORD_CHANGED,
        /** 所属租户被冻结 */
        TENANT_FROZEN,
        /** 所属租户被删除 */
        TENANT_DELETED
    }
}
