package tech.yunyue.core.rabbitmq.log;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.base.LogSupporter;
import tech.yunyue.core.log.base.LoginlogEntity;
import tech.yunyue.core.log.config.LogConfig;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.log.events.ErrorLogEvent;
import tech.yunyue.core.log.events.LoginLogEvent;
import tech.yunyue.core.properties.BizLog;
import tech.yunyue.core.properties.ErrorLog;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.properties.LoginLog;
import tech.yunyue.core.rabbitmq.log.supporter.MQLogSupporter;

import java.util.Optional;

@Configuration
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


    @ConditionalOnProperty(name = "eva.bizlog.mq-enabled", havingValue = "true")
    @Bean
    public Object bizlog() {
        // 与LogConfig接口的bizLogSupporter方法同名 因为要替换掉同名Supporter
        String beanName = BIZ_LOG_NAME;
        BizLog.Rabbit rabbit = Optional.ofNullable(evaConfig.getBizlog().getRabbit()).orElse(new BizLog.Rabbit());
        MQLogSupporter rabbitLogSupporter = new MQLogSupporter(rabbitTemplate, SpringUtil.getBean(beanName), rabbit.getExchange(), rabbit.getRoutingKey());
        //替换bean 先删除再注册同名bean
        beanFactory.removeBeanDefinition(beanName);
        beanFactory.registerSingleton(beanName,rabbitLogSupporter);
        return null;
    }
    @ConditionalOnProperty(name = "eva.errorlog.mq-enabled", havingValue = "true")
    @Bean
    public Object errorlog() {
        // 与LogConfig接口的errorLogSupporter方法同名 因为要替换掉同名Supporter
        String beanName = ERROR_LOG_NAME;
        ErrorLog.Rabbit rabbit = Optional.ofNullable(evaConfig.getErrorLog().getRabbit()).orElse(new ErrorLog.Rabbit());
        MQLogSupporter rabbitLogSupporter = new MQLogSupporter(rabbitTemplate, SpringUtil.getBean(beanName), rabbit.getExchange(), rabbit.getRoutingKey());
        //替换bean 先删除再注册同名bean
        beanFactory.removeBeanDefinition(beanName);
        beanFactory.registerSingleton(beanName,rabbitLogSupporter);
        return null;
    }

    @ConditionalOnProperty(name = "eva.loginlog.mq-enabled", havingValue = "true")
    @Bean
    public Object loginlog() {
        // 与LogConfig接口的errorLogSupporter方法同名 因为要替换掉同名Supporter
        String beanName = LOGIN_LOG_NAME;
        LoginLog.Rabbit rabbit = Optional.ofNullable(evaConfig.getLoginLog().getRabbit()).orElse(new LoginLog.Rabbit());
        MQLogSupporter rabbitLogSupporter = new MQLogSupporter(rabbitTemplate, SpringUtil.getBean(beanName), rabbit.getExchange(), rabbit.getRoutingKey());
        //替换bean 先删除再注册同名bean
        beanFactory.removeBeanDefinition(beanName);
        beanFactory.registerSingleton(beanName,rabbitLogSupporter);
        return null;
    }

}
