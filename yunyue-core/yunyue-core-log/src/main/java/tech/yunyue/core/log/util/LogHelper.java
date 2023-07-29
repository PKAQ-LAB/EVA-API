package tech.yunyue.core.log.util;

import cn.hutool.extra.spring.SpringUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.base.LogSupporter;
import tech.yunyue.core.log.base.LoginlogEntity;
import tech.yunyue.core.log.config.LogConfig;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.log.events.ErrorLogEvent;
import tech.yunyue.core.log.events.LoginLogEvent;

@Component
public class LogHelper {
    @Autowired
    ApplicationEventPublisher publisher;
    private static ApplicationEventPublisher eventPublisher;
    public static LogSupporter bizLogSupporter;
    public static LogSupporter errorLogSupporter;
    public static LogSupporter lopginLogSupporter;
    @PostConstruct
    public void init(){
        bizLogSupporter = SpringUtil.getBean(LogConfig.BIZ_LOG_NAME);
        errorLogSupporter = SpringUtil.getBean(LogConfig.ERROR_LOG_NAME);
        lopginLogSupporter = SpringUtil.getBean(LogConfig.LOGIN_LOG_NAME);
        eventPublisher = publisher;
    }

    public static void save(BizLogEntity bizLogEntity){
        eventPublisher.publishEvent(new BizLogEvent(bizLogEntity));
    }
    public static void save(ErrorlogEntity errorlogEntity){
        eventPublisher.publishEvent(new ErrorLogEvent(errorlogEntity));
    }
    public static void save(LoginlogEntity loginlogEntity){
        eventPublisher.publishEvent(new LoginLogEvent(loginlogEntity));
    }
}
