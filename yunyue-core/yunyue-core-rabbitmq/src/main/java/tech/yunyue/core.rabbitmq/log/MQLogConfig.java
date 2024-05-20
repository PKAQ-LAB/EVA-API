package tech.yunyue.core.rabbitmq.log;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.yunyue.core.log.base.*;
import tech.yunyue.core.log.config.LogConfig;
import tech.yunyue.core.log.constant.LogConstant;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.log.events.ErrorLogEvent;
import tech.yunyue.core.log.events.LoginLogEvent;
import tech.yunyue.core.log.events.ReportLogEvent;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.rabbitmq.log.supporter.MQLogSupporter;


@Configuration
@ConditionalOnExpression("${eva.syslog.mq-enabled:false}")
public class MQLogConfig implements LogConfig {
    @Autowired
    DefaultListableBeanFactory beanFactory;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    EvaConfig evaConfig;

    @Override
    public <T extends BizLogEntity> LogSupporter<T, BizLogEvent> bizLogSupporter() {
        return null;
    }

    @Override
    public <T extends ErrorlogEntity> LogSupporter<T, ErrorLogEvent> errorLogSupporter() {
        return null;
    }

    @Override
    public <T extends LoginlogEntity> LogSupporter<T, LoginLogEvent> loginLogSupporter() {
        return null;
    }

    @Override
    public <T extends ReportLogEntity> LogSupporter<T, ReportLogEvent> reportLogSupporter() {
        return null;
    }

    @Bean
    public Object bizlog() {
        // 与LogConfig接口的bizLogSupporter方法同名 因为要替换掉同名Supporter
        String beanName = BIZ_LOG_NAME;
        MQLogSupporter rabbitLogSupporter = new MQLogSupporter(rabbitTemplate, SpringUtil.getBean(beanName), LogConstant.LOG_EXCHANGE, LogConstant.BIZ_LOG_ROUTINGKEY);
        // 替换bean 先删除再注册同名bean
        beanFactory.removeBeanDefinition(beanName);
        beanFactory.registerSingleton(beanName, rabbitLogSupporter);
        return null;
    }

    @Bean
    public Object errorlog() {
        // 与LogConfig接口的errorLogSupporter方法同名 因为要替换掉同名Supporter
        String beanName = ERROR_LOG_NAME;
        MQLogSupporter rabbitLogSupporter = new MQLogSupporter(rabbitTemplate, SpringUtil.getBean(beanName), LogConstant.LOG_EXCHANGE, LogConstant.ERROR_LOG_ROUTINGKEY);
        // 替换bean 先删除再注册同名bean
        beanFactory.removeBeanDefinition(beanName);
        beanFactory.registerSingleton(beanName, rabbitLogSupporter);
        return null;
    }

    @Bean
    public Object loginlog() {
        // 与LogConfig接口的errorLogSupporter方法同名 因为要替换掉同名Supporter
        String beanName = LOGIN_LOG_NAME;
        MQLogSupporter rabbitLogSupporter = new MQLogSupporter(rabbitTemplate, SpringUtil.getBean(beanName), LogConstant.LOG_EXCHANGE, LogConstant.LOGIN_LOG_ROUTINGKEY);
        // 替换bean 先删除再注册同名bean
        beanFactory.removeBeanDefinition(beanName);
        beanFactory.registerSingleton(beanName, rabbitLogSupporter);
        return null;
    }

    @Bean
    public Object reportlog() {
        // 与LogConfig接口的errorLogSupporter方法同名 因为要替换掉同名Supporter
        String beanName = REPORT_LOG_NAME;
        MQLogSupporter rabbitLogSupporter = new MQLogSupporter(rabbitTemplate, SpringUtil.getBean(beanName), LogConstant.LOG_EXCHANGE, LogConstant.REPORT_LOG_ROUTINGKEY);
        // 替换bean 先删除再注册同名bean
        beanFactory.removeBeanDefinition(beanName);
        beanFactory.registerSingleton(beanName, rabbitLogSupporter);
        return null;
    }


}
