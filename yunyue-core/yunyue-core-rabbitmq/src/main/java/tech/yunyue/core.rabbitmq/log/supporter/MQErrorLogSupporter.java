package tech.yunyue.core.rabbitmq.log.supporter;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tech.yunyue.core.log.base.ErrorLogSupporter;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.events.ErrorLogEvent;
import tech.yunyue.core.properties.ErrorLog;
import tech.yunyue.core.properties.EvaConfig;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class MQErrorLogSupporter implements ErrorLogSupporter {
    private final ErrorLogSupporter logSupporter;
    private final RabbitTemplate rabbitTemplate;
    private final EvaConfig evaConfig;

    @Override
    public void save(ErrorlogEntity errorlogEntity) {
        this.logSupporter.save(errorlogEntity);
    }

    /**
     * 发送到mq
     * @param event
     */
    @Override
    public void listener(ErrorLogEvent event) {
        ErrorlogEntity logEntity = (ErrorlogEntity) event.getSource();
        this.convertAndSend(logEntity);
    }

    @Override
    public List<? extends ErrorlogEntity> getLog() {
        return logSupporter.getLog();
    }

    @Override
    public List<? extends ErrorlogEntity> getLogAfter(Date dateTime) {
        return logSupporter.getLogAfter(dateTime);
    }

    @Override
    public List<? extends ErrorlogEntity> getLogBetween(Date begin, Date end) {
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

    private void convertAndSend(ErrorlogEntity errorlogEntity){
        ErrorLog.Rabbit rabbit = evaConfig.getErrorLog().getRabbit();
        rabbitTemplate.convertAndSend(rabbit.getBizExchange(), rabbit.getBizRoutingKey(), errorlogEntity);
    }
}
