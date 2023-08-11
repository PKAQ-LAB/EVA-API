package tech.yunyue.core.mybatis.log;

import com.nimbusds.jose.shaded.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import tech.yunyue.core.log.base.LogSupporter;
import tech.yunyue.core.log.condition.MybatisSupporterCondition;
import tech.yunyue.core.log.config.LogConfig;
import tech.yunyue.core.log.constant.LogConstant;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.log.events.ErrorLogEvent;
import tech.yunyue.core.log.events.LoginLogEvent;
import tech.yunyue.core.mybatis.log.error.entity.MybatisErrorlogEntity;
import tech.yunyue.core.mybatis.log.error.mapper.ErrorlogMapper;
import tech.yunyue.core.mybatis.log.biz.entity.MybatisBizLogEntity;
import tech.yunyue.core.mybatis.log.biz.mapper.MybatisSupporterMapper;
import tech.yunyue.core.mybatis.log.login.entity.MybatisLoginLogEntity;
import tech.yunyue.core.mybatis.log.login.mapper.MybatisLoginLogMapper;
import tech.yunyue.core.properties.EvaConfig;

@Configuration
@Conditional(MybatisSupporterCondition.class)
public class MybatisLogConfig implements LogConfig {
    @Autowired
    EvaConfig evaConfig;
    @Autowired
    MybatisSupporterMapper bizLogMapper;
    @Autowired
    ErrorlogMapper errorLogMapper;
    @Autowired
    MybatisLoginLogMapper loginLogMapper;

    @Override
    @Bean
    public LogSupporter<MybatisBizLogEntity, BizLogEvent> bizLogSupporter() {
        return new MybatisLogSupporter<>(new TypeToken<MybatisLogSupporter<MybatisBizLogEntity, BizLogEvent>>(){}, bizLogMapper, LogConstant.BIZ_LOG_DATE_FIELD);
    }

    @Override
    @Bean
    public LogSupporter<MybatisErrorlogEntity, ErrorLogEvent> errorLogSupporter() {
        return new MybatisLogSupporter<>(new TypeToken<MybatisLogSupporter<MybatisErrorlogEntity, ErrorLogEvent>>(){}, errorLogMapper, LogConstant.ERROR_LOG_DATE_FIELD);
    }

    @Bean
    @Override
    public LogSupporter<MybatisLoginLogEntity, LoginLogEvent> loginLogSupporter() {
        return new MybatisLogSupporter<>(new TypeToken<MybatisLogSupporter<MybatisLoginLogEntity, LoginLogEvent>>(){}, loginLogMapper, LogConstant.LOGIN_LOG_DATE_FIELD);
    }
}
