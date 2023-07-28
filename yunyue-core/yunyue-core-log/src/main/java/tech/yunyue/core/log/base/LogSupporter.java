package tech.yunyue.core.log.base;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tech.yunyue.core.log.events.LogEvent;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 日志持久化接口
 *
 * @author: S.PKAQ
 */
public interface LogSupporter<T extends LogEntity, E extends LogEvent> {

    /**
     * 保存日志
     */
    void save(T t);

    /**
     * 获取日志
     */
    List<? extends T> getLog();

    /**
     * 获取指定操作类型的日志
     *
     * @param type 操作类型
     * @return 符合条件的结果集
     */
    List<? extends T> getLogByType(String type);

    /**
     * 监听有事务且成功提交 或者 没有事务<br/>
     * 异步保存操作日志
     */
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT,fallbackExecution=true)
    @Async("log_task")
    default void listenerCommit(E event) {
//        Map<String, Object> source = (Map<String, Object>) event.getSource();
//        T logEntity = (T) source.get(LogConstant.TRANSACTIONAL_LOG);
//        if(Objects.isNull(logEntity)){
//            logEntity =  (T) source.get(LogConstant.EVENT_LOG);
//        }
//        this.save(logEntity);
//        if()
        // 接收的类型匹配才进行保存操作
        if(checkMatch(event))  this.save( (T) event.getSource());
    }

    /**
     * 监听存在事务且事务回滚的BizLogEvent事件<br/>
     * 异步保存失败操作日志
     */
    @TransactionalEventListener(phase= TransactionPhase.AFTER_ROLLBACK)
    @Async("log_task")
     default void listenerRollbask(E event) {
//        Map<String, Object> source = (Map<String, Object>) event.getSource();
//        T logEntity = (T) source.get(LogConstant.TRANSACTIONAL_LOG);
//        if(Objects.nonNull(logEntity)){
//            logEntity.setDescription("【操作失败】"+logEntity.getDescription());
//            this.save(logEntity);
//        }
        T logEntity = (T) event.getSource();
        if(Objects.nonNull(logEntity)){
            try {
                // 给日志的描述加上失败标记
                Field field = logEntity.getClass().getField("description");
                field.setAccessible(true);
                String des = StrUtil.toStringOrNull(field.get(logEntity));
                field.set(logEntity, "【操作失败】"+ des);
            } catch (Exception ignored){
            } finally {
                // 接收的类型匹配才进行保存操作
                if (checkMatch(event))  this.save(logEntity);
            }
        }

    }

    private boolean checkMatch(E event) {
        try {
            var actualTypeArguments = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments();
            var actualTypeE = actualTypeArguments[1].getTypeName();
            var parameE = event.getClass().getName();
            return actualTypeE.contains(parameE);
        } catch (Exception ignored){}
        return false;
    }

    default Class getActualTClass() {
        try {
            var actualTypeArguments = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments();
            return (Class)actualTypeArguments[0];
        } catch (Exception ignored){}
        return null;
    }
    default Object getActualTObj(T t) {
        try {
            var actualTypeArguments = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments();
            var clazz = (Class)actualTypeArguments[0];
            var actualObj = clazz.getDeclaredConstructor().newInstance();
            BeanUtil.copyProperties(t, actualObj);
            return actualObj;
        } catch (Exception ignored){}
        return null;
    }

    /**
     * 获取某个时间之后的日志
     *
     * @param dateTime 时间点
     * @return 符合条件的日志集合
     */
    List<? extends T> getLogAfter(Date dateTime);

    /**
     * 获取某个日期区间的日志
     *
     * @param begin 开始日期区间
     * @param end   结束日期区间
     * @return 所查询区间的日志
     */
    List<? extends T> getLogBetween(Date begin, Date end);

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
