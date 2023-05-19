package tech.yunyue.core.log.base;

import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tech.yunyue.core.log.constant.LogConstant;
import tech.yunyue.core.log.events.BizLogEvent;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 业务日志持久化接口
 *
 * @author: S.PKAQ
 */
public interface BizLogSupporter {
    /**
     * 保存日志
     */
    void save(BizLogEntity bizLogEntity);

    /**
     * 获取日志
     */
    List<? extends BizLogEntity> getLog();

    /**
     * 获取指定操作类型的日志
     *
     * @param type 操作类型
     * @return 符合条件的结果集
     */
    List<? extends BizLogEntity> getLogByType(String type);

    /**
     * 监听有事务且成功提交 或者 没有事务<br/>
     * 异步保存操作日志
     */
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT,fallbackExecution=true)
    @Async("log_task")
    default void listenerCommit(BizLogEvent event) {
        Map<String, Object> source = (Map<String, Object>) event.getSource();
        BizLogEntity logEntity = (BizLogEntity) source.get(LogConstant.TRANSACTIONAL_LOG);
        if(Objects.isNull(logEntity)){
            logEntity =  (BizLogEntity) source.get(LogConstant.EVENT_LOG);
        }
        this.save(logEntity);
    }

    /**
     * 监听存在事务且事务回滚的BizLogEvent事件<br/>
     * 异步保存失败操作日志
     */
    @TransactionalEventListener(phase= TransactionPhase.AFTER_ROLLBACK)
    @Async("log_task")
     default void listenerRollbask(BizLogEvent event) {
        Map<String, Object> source = (Map<String, Object>) event.getSource();
        BizLogEntity logEntity = (BizLogEntity) source.get(LogConstant.TRANSACTIONAL_LOG);
        if(Objects.nonNull(logEntity)){
            logEntity.setDescription("【操作失败】"+logEntity.getDescription());
            this.save(logEntity);
        }
    }

    /**
     * 获取某个时间之后的日志
     *
     * @param dateTime 时间点
     * @return 符合条件的日志集合
     */
    List<? extends BizLogEntity> getLogAfter(Date dateTime);

    /**
     * 获取某个日期区间的日志
     *
     * @param begin 开始日期区间
     * @param end   结束日期区间
     * @return 所查询区间的日志
     */
    List<? extends BizLogEntity> getLogBetween(Date begin, Date end);

    /**
     * 清除所有日志
     */
    void cleanAll();

    /**
     * 清除某个时间点之前的日志
     *
     * @param dateTime 要清除的时间点
     */
    void cleanBefore(Date dateTime);

    /**
     * 清除某个时间区间的日志
     *
     * @param begin 开始时间
     * @param end   结束时间
     */
    void cleanBetween(Date begin, Date end);

    /**
     * 打印当前操作日志
     */
    void print();
}
