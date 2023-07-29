package tech.yunyue.core.log.util;

import cn.hutool.extra.spring.SpringUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.base.LogSupporter;
import tech.yunyue.core.log.config.LogConfig;

@Component
public class LogHelper {
    @Autowired
    ApplicationEventPublisher publisher;
    private static LogSupporter bizLogSupporter;
    private static LogSupporter errorLogSupporter;
    @PostConstruct
    public void init(){
        bizLogSupporter = SpringUtil.getBean(LogConfig.BIZ_LOG_NAME);
        errorLogSupporter = SpringUtil.getBean(LogConfig.ERROR_LOG_NAME);
    }

    public static void save(BizLogEntity bizLogEntity){
        bizLogSupporter.save(bizLogEntity);
    }

    public static void save(ErrorlogEntity errorlogEntity){
        errorLogSupporter.save(errorlogEntity);
    }
}
