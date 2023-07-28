package tech.yunyue.core.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.yunyue.core.properties.EvaConfig;

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
}
