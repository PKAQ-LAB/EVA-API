package tech.yunyue.core.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import tech.yunyue.core.log.base.BizLogSupporter;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.rabbitmq.log.supporter.MQLogSupporter;
import tech.yunyue.core.util.StringPool;

@Configuration
@Slf4j
public class RabbitConfig {
    @Autowired
    EvaConfig evaConfig;
    // rabbitmq默认的基于JDK做的序列化
    // Spring的队列消息对象的处理是由 MessageConverter 来处理的，而默认实现是 SimpleMessageConverter，该类基于 JDK 的 ObjectOutputStream 完成序列化。
    // 序列化
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 用mq的日志代理对象替换原有日志处理对象
    @Bean
    public Object rabbitLogSupporter(DefaultListableBeanFactory beanFactory,
                                     BizLogSupporter bizLogSupporter,
                                     RabbitTemplate rabbitTemplate) {
        String className = AopUtils.getTargetClass(bizLogSupporter).getName();
        String beanName = StringUtils.uncapitalize(className.substring(className.lastIndexOf(StringPool.DOT)+1));
        MQLogSupporter rabbitLogSupporter = new MQLogSupporter(bizLogSupporter, rabbitTemplate, evaConfig);
        //替换bean 先删除再注册同名bean
        beanFactory.removeBeanDefinition(beanName);
        beanFactory.registerSingleton(beanName,rabbitLogSupporter);
        return new Object();
    }
}
