package org.pkaq.core.log.base;

import org.pkaq.core.log.events.BizLogEvent;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.util.ReflectUtils;
import org.pkaq.core.util.StrUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 日志持久化接口
 * 所有日志存储实现（Console、Jdbc、Mybatis、MongoDB等）均需实现此接口
 *
 * @author PKAQ
 */
public interface LogSupporter {
    String FAILURE_PREFIX = "【操作失败】";

    /**
     * 保存日志
     *
     * @param bizLogEntity 业务日志实体
     */
    void save(BizLogEntity bizLogEntity);

    /**
     * 根据id获取日志详情
     *
     * @param id 日志id
     * @return 日志详情JSON
     */
    String get(String id);

    /**
     * 获取日志列表(分页)
     *
     * @param dateRangeBo 日期范围查询参数
     * @return 分页结果
     */
    Object list(DateRangeBo dateRangeBo);

    /**
     * 获取所有日志
     *
     * @return 日志列表
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

    /**
     * 监听有事务且成功提交 或者 没有事务
     * 异步保存操作日志
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Async("log_task")
    default void listenerCommit(BizLogEvent event) {
        this.save((BizLogEntity) event.getSource());
    }

    /**
     * 监听存在事务且事务回滚的BizLogEvent事件
     * 异步保存失败操作日志
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Async("log_task")
    default void listenerRollback(BizLogEvent event) {
        BizLogEntity logEntity = (BizLogEntity) event.getSource();
        if (Objects.nonNull(logEntity)) {
            try {
                // 给日志的描述加上失败标记
                Field field = ReflectUtils.getField(logEntity.getClass(), "description");
                String des = StrUtils.toStringOrNull(ReflectUtils.getFieldValue(logEntity, field));
                field.set(logEntity, "%s%s".formatted(FAILURE_PREFIX, des));
            } catch (Exception ignored) {
                // 设置参数失败，不处理
            } finally {
                this.save(logEntity);
            }
        }
    }
}
