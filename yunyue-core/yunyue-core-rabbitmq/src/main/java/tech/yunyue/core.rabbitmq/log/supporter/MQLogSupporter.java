package tech.yunyue.core.rabbitmq.log.supporter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tech.yunyue.core.log.base.LogEntity;
import tech.yunyue.core.log.base.LogSupporter;
import tech.yunyue.core.log.base.bo.LogQueryBo;
import tech.yunyue.core.log.events.LogEvent;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class MQLogSupporter<T extends LogEntity, E extends LogEvent> implements LogSupporter<T, E> {
    private final LogSupporter<T, E> logSupporter;
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public MQLogSupporter(RabbitTemplate rabbitTemplate, LogSupporter<T, E> logSupporter, String exchange, String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.logSupporter = logSupporter;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public Type[] getRealTE() {
        return logSupporter.getRealTE();
    }

    @Override
    public void save(T t) {
        this.logSupporter.save(t);
    }

    /**
     * 发送到mq
     *
     * @param event
     */
    @Override
    public void listenerCommit(E event) {
        T entity = (T) event.getSource();
        if (checkMatch(event)) this.convertAndSend(entity);
    }

    /**
     * 发送到mq
     *
     * @param event
     */
    @Override
    public void listenerRollback(E event) {
        T entity = (T) event.getSource();
        if (checkMatch(event)) this.convertAndSend(entity);
    }

    @Override
    public List<? extends T> getLog() {
        return logSupporter.getLog();
    }

    @Override
    public T getLogById(String id) {
        return logSupporter.getLogById(id);
    }

    @Override
    public IPage<T> getLogByQuery(LogQueryBo queryBo) {
        return logSupporter.getLogByQuery(queryBo);
    }

    @Override
    public List<? extends T> getLogByType(String type) {
        return logSupporter.getLogByType(type);
    }

    @Override
    public List<? extends T> getLogAfter(Date dateTime) {
        return logSupporter.getLogAfter(dateTime);
    }

    @Override
    public List<? extends T> getLogBetween(Date begin, Date end) {
        return logSupporter.getLogBetween(begin, end);
    }

    @Override
    public void cleanAll() {
        logSupporter.cleanAll();
    }

    @Override
    public void cleanBefore(Date dateTime) {
        logSupporter.cleanBefore(dateTime);
    }

    @Override
    public void cleanBetween(Date begin, Date end) {
        logSupporter.cleanBetween(begin, end);
    }

    @Override
    public void print() {
        logSupporter.print();
    }

    private void convertAndSend(T t) {
        rabbitTemplate.convertAndSend(this.exchange, this.routingKey, t);
    }
}
