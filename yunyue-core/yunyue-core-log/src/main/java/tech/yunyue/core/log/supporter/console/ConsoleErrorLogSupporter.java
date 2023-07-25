package tech.yunyue.core.log.supporter.console;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.base.ErrorLogSupporter;
import tech.yunyue.core.log.condition.DefaultErrorLogSupporterCondition;

import java.util.Date;
import java.util.List;

/**
 * 控制台错误日志实现类
 */
@Slf4j
@Component
@Conditional(DefaultErrorLogSupporterCondition.class)
public class ConsoleErrorLogSupporter implements ErrorLogSupporter {
    private ErrorlogEntity errorlogEntity;

    public ConsoleErrorLogSupporter() {
        super();
    }

    public ConsoleErrorLogSupporter(ErrorlogEntity errorlogEntity) {
        this.errorlogEntity = errorlogEntity;
    }

    @Override
    public void save(ErrorlogEntity errorlogEntity) {
        log.info(errorlogEntity.toString());
    }

    @Override
    public List<ErrorlogEntity> getLog() {
        return null;
    }

    @Override
    public List<ErrorlogEntity> getLogAfter(Date dateTime) {
        return null;
    }

    @Override
    public List<ErrorlogEntity> getLogBetween(Date begin, Date end) {
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
        log.info("error log: " + this.errorlogEntity.getExDesc());
    }
}
