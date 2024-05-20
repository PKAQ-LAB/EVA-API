package tech.yunyue.core.log.supporter.console;

import com.nimbusds.jose.shaded.gson.reflect.TypeToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import tech.yunyue.core.log.base.*;
import tech.yunyue.core.log.condition.DefaultSupporterCondition;
import tech.yunyue.core.log.config.LogConfig;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.log.events.ErrorLogEvent;
import tech.yunyue.core.log.events.LoginLogEvent;
import tech.yunyue.core.log.events.ReportLogEvent;

@Configuration
@Conditional(DefaultSupporterCondition.class)
public class ConsoleLogConfig implements LogConfig {

    @Bean
    @Override
    public LogSupporter<BizLogEntity, BizLogEvent> bizLogSupporter(){
        // 为了拿到实际事件类型 否则多个LogSupporter都会触发事件 会保存多次
        return new ConsoleSupporter<BizLogEntity, BizLogEvent>(new TypeToken<ConsoleSupporter<BizLogEntity, BizLogEvent>>(){});
    }

    @Bean
    @Override
    public LogSupporter<ErrorlogEntity, ErrorLogEvent> errorLogSupporter(){
        return new ConsoleSupporter<ErrorlogEntity, ErrorLogEvent>(new TypeToken<ConsoleSupporter<ErrorlogEntity, ErrorLogEvent>>(){});
    }

    @Bean
    @Override
    public LogSupporter<LoginlogEntity, LoginLogEvent> loginLogSupporter(){
        return new ConsoleSupporter<LoginlogEntity, LoginLogEvent>(new TypeToken<ConsoleSupporter<LoginlogEntity, LoginLogEvent>>(){});
    }

    @Override
    public LogSupporter<ReportLogEntity, ReportLogEvent> reportLogSupporter() {
        return new ConsoleSupporter<ReportLogEntity, ReportLogEvent>(new TypeToken<ConsoleSupporter<ReportLogEntity, ReportLogEvent>>(){});
    }
}
