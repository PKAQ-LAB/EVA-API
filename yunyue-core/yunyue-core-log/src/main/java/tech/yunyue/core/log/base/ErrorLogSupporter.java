package tech.yunyue.core.log.base;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import tech.yunyue.core.log.events.ErrorLogEvent;

import java.util.Date;
import java.util.List;

/**
 * 错误日志持久化接口
 */
public interface ErrorLogSupporter {
    /**
     * 保存日志
     */
    void save(ErrorlogEntity errorlogEntity);

    /**
     * 获取日志
     */
    List<? extends ErrorlogEntity> getLog();

    /**
     * 异步保存错误日志
     */
    @EventListener
    @Async("log_task")
    default void listener(ErrorLogEvent event) {
        ErrorlogEntity logEntity = (ErrorlogEntity) event.getSource();
        this.save(logEntity);
    }

    /**
     * 获取某个时间之后的日志
     *
     * @param dateTime 时间点
     * @return 符合条件的日志集合
     */
    List<? extends ErrorlogEntity> getLogAfter(Date dateTime);

    /**
     * 获取某个日期区间的日志
     *
     * @param begin 开始日期区间
     * @param end   结束日期区间
     * @return 所查询区间的日志
     */
    List<? extends ErrorlogEntity> getLogBetween(Date begin, Date end);

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
     * 打印当前错误日志
     */
    void print();
}
