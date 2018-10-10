package org.pkaq.core.bizlog.supporter.console;

import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.bizlog.base.BizLogEntity;
import org.pkaq.core.bizlog.base.BizLogEnum;
import org.pkaq.core.bizlog.base.BizLogSupporter;
import org.pkaq.core.bizlog.condition.MybatisSupporterCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 控制台日志实现类
 * @author: S.PKAQ
 * @Datetime: 2018/9/26 21:48
 */
@Slf4j
@Component
@Conditional(MybatisSupporterCondition.class)
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
