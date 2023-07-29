package tech.yunyue.core.log.supporter.console;

import com.nimbusds.jose.shaded.gson.reflect.TypeToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.base.LogSupporter;
import tech.yunyue.core.log.base.LoginlogEntity;
import tech.yunyue.core.log.condition.DefaultErrorLogSupporterCondition;
import tech.yunyue.core.log.condition.DefaultLoginLogSupporterCondition;
import tech.yunyue.core.log.condition.DefaultSupporterCondition;
import tech.yunyue.core.log.config.LogConfig;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.log.events.ErrorLogEvent;
import tech.yunyue.core.log.events.LoginLogEvent;

@Configuration
public class ConsoleLogConfig implements LogConfig {

    @Conditional(DefaultSupporterCondition.class)
    @Bean
    @Override
    public LogSupporter<BizLogEntity, BizLogEvent> bizLogSupporter(){
        // 为了拿到实际事件类型 否则多个LogSupporter都会触发事件 会保存多次
        return new ConsoleSupporter<BizLogEntity, BizLogEvent>(new TypeToken<ConsoleSupporter<BizLogEntity, BizLogEvent>>(){});
    }

    @Conditional(DefaultErrorLogSupporterCondition.class)
    @Bean
    @Override
    public LogSupporter<ErrorlogEntity, ErrorLogEvent> errorLogSupporter(){
        return new ConsoleSupporter<ErrorlogEntity, ErrorLogEvent>(new TypeToken<ConsoleSupporter<ErrorlogEntity, ErrorLogEvent>>(){});
    }

    @Conditional(DefaultLoginLogSupporterCondition.class)
    @Bean
    @Override
    public LogSupporter<LoginlogEntity, LoginLogEvent> loginLogSupporter(){
        return new ConsoleSupporter<LoginlogEntity, LoginLogEvent>(new TypeToken<ConsoleSupporter<LoginlogEntity, LoginLogEvent>>(){});
    }
}
