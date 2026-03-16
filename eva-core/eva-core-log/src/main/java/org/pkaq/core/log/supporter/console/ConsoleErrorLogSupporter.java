package org.pkaq.core.log.supporter.console;

import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.log.base.ErrorLogEntity;
import org.pkaq.core.log.base.ErrorLogSupporter;
import org.pkaq.core.log.condition.DefaultErrorlogSupporterCondition;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mvc.vo.PageVo;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * 控制台错误日志处理器
 *
 * @author PKAQ
 */
@Slf4j
@Component
@Conditional(DefaultErrorlogSupporterCondition.class)
public class ConsoleErrorLogSupporter implements ErrorLogSupporter {

    @Override
    public void save(ErrorLogEntity entity) {
        log.error(entity.toString());
    }

    @Override
    public ErrorLogEntity get(String id) {
        log.warn("ConsoleErrorLogSupporter不支持查询操作");
        return null;
    }

    @Override
    public PageVo<? extends ErrorLogEntity> list(DateRangeBo dateRangeBo, int pageNo, int pageSize) {
        log.warn("ConsoleErrorLogSupporter不支持查询操作");
        return new PageVo<>();
    }
}
