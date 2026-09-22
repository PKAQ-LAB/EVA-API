package org.pkaq.core.log.events;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.log.base.BizLogEntity;
import org.pkaq.core.log.base.BusinessLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 业务日志事件处理器。
 *
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class BusinessLogEventHandler {
    public static final String FAILURE_PREFIX = "【操作失败】";

    private final BusinessLogRepository businessLogRepository;

    /**
     * 事务提交后异步保存业务日志。
     *
     * @param event 业务日志事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Async("log_task")
    public void listenerCommit(BizLogEvent event) {
        this.businessLogRepository.save((BizLogEntity) event.getSource());
    }

    /**
     * 事务回滚后为业务日志标记失败并异步保存。
     *
     * @param event 业务日志事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Async("log_task")
    public void listenerRollback(BizLogEvent event) {
        BizLogEntity logEntity = (BizLogEntity) event.getSource();
        if (logEntity == null) {
            return;
        }
        logEntity.setDescription(FAILURE_PREFIX + logEntity.getDescription());
        this.businessLogRepository.save(logEntity);
    }
}
