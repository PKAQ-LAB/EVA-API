package tech.yunyue.core.log.supporter.console;

import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.BizLogSupporter;
import tech.yunyue.core.log.condition.DefaultSupporterCondition;
import tech.yunyue.core.log.constant.LogConstant;
import tech.yunyue.core.log.events.BizLogEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 控制台日志实现类
 *
 * @author PKAQ
 */
@Slf4j
@Component
@Conditional(DefaultSupporterCondition.class)
public class ConsoleSupporter implements BizLogSupporter {
    private BizLogEntity bizLogEntity;

    public ConsoleSupporter() {
        super();
    }

    public ConsoleSupporter(BizLogEntity bizLogEntity) {
        this.bizLogEntity = bizLogEntity;
    }

    @Override
    public void save(BizLogEntity bizLogEntity) {

    }

    /**
     * 日志保存
     *
     * @param event
     */
    @Async
    @EventListener(value = BizLogEvent.class)
    public void listener(BizLogEvent event) {
        Map<String, Object> source = (Map<String, Object>) event.getSource();
        BizLogEntity errorEntity = (BizLogEntity) source.get(LogConstant.EVENT_LOG);
        this.save(errorEntity);
    }

    @Override
    public List<BizLogEntity> getLog() {
        return null;
    }

    @Override
    public List<BizLogEntity> getLogByType(String type) {
        return null;
    }

    @Override
    public List<BizLogEntity> getLogAfter(Date dateTime) {
        return null;
    }

    @Override
    public List<BizLogEntity> getLogBetween(Date begin, Date end) {
        return null;
    }

    @Override
    public void cleanAll() {

    }

    @Override
    public void cleanBefore(Date dateTime) {

    }

    @Override
    public void cleanBetween(Date begin, Date end) {

    }

    @Override
    public void print() {
        log.info("Biz log: " + this.bizLogEntity.getDescription());
    }
}
