package io.nerv.core.bizlog.supporter.console;

import io.nerv.core.bizlog.base.BizLogEntity;
import io.nerv.core.bizlog.base.BizLogSupporter;
import io.nerv.core.bizlog.condition.DefaultSupporterCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 控制台日志实现类
 * @author: S.PKAQ
 */
@Slf4j
@Component
@Conditional(DefaultSupporterCondition.class)
public class ConsoleSupporter implements BizLogSupporter {
    private BizLogEntity bizLogEntity;

    public ConsoleSupporter() {
        super();
    }

    public ConsoleSupporter(BizLogEntity  bizLogEntity) {
        this.bizLogEntity = bizLogEntity;
    }

    @Override
    public void save(BizLogEntity bizLogEntity) {

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
