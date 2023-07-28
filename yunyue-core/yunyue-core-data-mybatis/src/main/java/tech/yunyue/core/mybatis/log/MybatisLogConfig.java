package tech.yunyue.core.mybatis.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import tech.yunyue.core.log.base.LogSupporter;
import tech.yunyue.core.log.condition.MybatisSupporterCondition;
import tech.yunyue.core.log.config.LogConfig;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.log.events.ErrorLogEvent;
import tech.yunyue.core.mybatis.log.error.entity.MybatisErrorlogEntity;
import tech.yunyue.core.mybatis.log.error.mapper.ErrorlogMapper;
import tech.yunyue.core.mybatis.log.biz.entity.MybatisBizLogEntity;
import tech.yunyue.core.mybatis.log.biz.mapper.MybatisSupporterMapper;
import tech.yunyue.core.properties.BizLog;
import tech.yunyue.core.properties.ErrorLog;
import tech.yunyue.core.properties.EvaConfig;

@Configuration
public class MybatisLogConfig implements LogConfig {
    @Autowired
    EvaConfig evaConfig;
    @Autowired
    MybatisSupporterMapper bizLogMapper;
    @Autowired
    ErrorlogMapper errorLogMapper;

    @Override
    @Conditional(MybatisSupporterCondition.class)
    @Bean
    public LogSupporter<MybatisBizLogEntity, BizLogEvent> bizLogSupporter() {
        BizLog bizLog = evaConfig.getBizlog();
        return new MybatisLogSupporter<MybatisBizLogEntity, BizLogEvent>(bizLogMapper, bizLog.getDateField()){};
    }

    @Override
    @ConditionalOnExpression("'mybatis'.equals('${eva.errorlog.impl}')")
    @Bean
    public LogSupporter<MybatisErrorlogEntity, ErrorLogEvent> errorLogSupporter() {
        ErrorLog errorLog = evaConfig.getErrorLog();
        return new MybatisLogSupporter<MybatisErrorlogEntity, ErrorLogEvent>(errorLogMapper, errorLog.getDateField()){};
    }
}
