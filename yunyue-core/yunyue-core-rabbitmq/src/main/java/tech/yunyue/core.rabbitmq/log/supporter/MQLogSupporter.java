package tech.yunyue.core.rabbitmq.log.supporter;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.BizLogSupporter;
import tech.yunyue.core.log.constant.LogConstant;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.properties.BizLog;
import tech.yunyue.core.properties.EvaConfig;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class MQLogSupporter implements BizLogSupporter {
    private final BizLogSupporter logSupporter;
    private final RabbitTemplate rabbitTemplate;
    private final EvaConfig evaConfig;

    @Override
    public void save(BizLogEntity bizLogEntity) {
        this.logSupporter.save(bizLogEntity);
    }

    /**
     * 发送到mq
     * @param event
     */
    @Override
    public void listenerCommit(BizLogEvent event) {
        Map<String, Object> source = (Map<String, Object>) event.getSource();
        BizLogEntity logEntity = (BizLogEntity) source.get(LogConstant.TRANSACTIONAL_LOG);
        if(Objects.isNull(logEntity)){
            logEntity =  (BizLogEntity) source.get(LogConstant.EVENT_LOG);
        }
        this.convertAndSend(logEntity);
    }

    /**
     * 发送到mq
     * @param event
     */
    @Override
    public void listenerRollbask(BizLogEvent event) {
        Map<String, Object> source = (Map<String, Object>) event.getSource();
        BizLogEntity logEntity = (BizLogEntity) source.get(LogConstant.TRANSACTIONAL_LOG);
        if(Objects.nonNull(logEntity)){
            logEntity.setDescription("【操作失败】"+logEntity.getDescription());
            this.convertAndSend(logEntity);
        }
    }

    @Override
    public List<? extends BizLogEntity> getLog() {
        return logSupporter.getLog();
    }

    @Override
    public List<? extends BizLogEntity> getLogByType(String type) {
        return logSupporter.getLogByType(type);
    }

    @Override
    public List<? extends BizLogEntity> getLogAfter(Date dateTime) {
        return logSupporter.getLogAfter(dateTime);
    }

    @Override
    public List<? extends BizLogEntity> getLogBetween(Date begin, Date end) {
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

    private void convertAndSend(BizLogEntity bizLogEntity){
        BizLog.Rabbit rabbit = evaConfig.getBizlog().getRabbit();
        rabbitTemplate.convertAndSend(rabbit.getExchange(), rabbit.getRoutingKey(), bizLogEntity);
    }
}
