package org.pkaq.core.log.supporter.console;

import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.log.base.BizLogEntity;
import org.pkaq.core.log.base.LogSupporter;
import org.pkaq.core.log.condition.DefaultSupporterCondition;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mvc.vo.PageVo;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 控制台日志实现类
 * 仅输出到控制台，不持久化，查询方法均返回空结果
 *
 * @author PKAQ
 */
@Slf4j
@Component
@Conditional(DefaultSupporterCondition.class)
public class ConsoleSupporter implements LogSupporter {

    @Override
    public void save(BizLogEntity bizLogEntity) {
        log.info(bizLogEntity.toString());
    }

    @Override
    public BizLogEntity get(String id) {
        log.warn("ConsoleSupporter不支持查询操作");
        return null;
    }

    @Override
    public PageVo<BizLogEntity> list(DateRangeBo dateRangeBo, int pageNo, int pageSize) {
        log.warn("ConsoleSupporter不支持查询操作");
        return new PageVo<>();
    }

    @Override
    public List<BizLogEntity> getLog() {
        return Collections.emptyList();
    }

    @Override
    public List<BizLogEntity> getLogByType(String type) {
        return Collections.emptyList();
    }

    @Override
    public List<BizLogEntity> getLogAfter(Date dateTime) {
        return Collections.emptyList();
    }

    @Override
    public List<BizLogEntity> getLogBetween(Date begin, Date end) {
        return Collections.emptyList();
    }

    @Override
    public void cleanAll() {
        // 控制台模式无需清理
    }

    @Override
    public void cleanBefore(Date dateTime) {
        // 控制台模式无需清理
    }

    @Override
    public void cleanBetween(Date begin, Date end) {
        // 控制台模式无需清理
    }

    @Override
    public void print() {
        log.info("ConsoleSupporter: print()");
    }
}
