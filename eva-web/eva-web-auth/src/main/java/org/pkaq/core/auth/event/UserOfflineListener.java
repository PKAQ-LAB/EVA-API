package org.pkaq.core.auth.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.event.UserOfflineEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 用户下线事件监听器
 * <p>
 * 业务侧（用户冻结/删除、租户冻结/删除、改密等）发布 {@link UserOfflineEvent}，
 * 由此监听器统一调用 {@link CacheTokenUtil#removeTokens} 清理 token。
 * <p>
 * 关键设计：
 * - {@code AFTER_COMMIT} 阶段触发：业务事务提交后才清 token，避免事务回滚后用户被无辜踢
 * - {@code fallbackExecution = true}：非事务上下文中也会触发（防御性）
 *
 * @author PKAQ
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserOfflineListener {
    private final CacheTokenUtil cacheTokenUtil;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserOffline(UserOfflineEvent event) {
        if (event.getUids() == null || event.getUids().isEmpty()) {
            return;
        }
        log.info("[token-evict] reason={}, uids={}", event.getReason(), event.getUids());
        cacheTokenUtil.removeTokens(event.getUids());
    }
}
