package tech.yunyue.core.rabbitmq.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.util.CollectionUtils;
import tech.yunyue.core.util.StringPool;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * rabbitmq 创建消息队列和交换机
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class RabbitMQAutoConfiguration {
    /**
     * Spring应用上下文环境
     */
    private ConfigurableListableBeanFactory beanFactory;
    private RabbitMQProperties properties;

    public RabbitMQProperties.ExchangeConfig getExchangeConfig(String name) {
        return properties.getExchanges().stream().filter(e -> e.getName().equals(name)).findFirst().orElse(null);
    }

    public RabbitMQAutoConfiguration(ConfigurableListableBeanFactory beanFactory, RabbitMQProperties properties) {
        this.beanFactory = beanFactory;
        this.properties = properties;
        // 创建交换机
        createExchange();
        // 绑定队列和交换机
        bindingQueueToExchange();

    }

    /**
     * 动态创建交换机
     */
    public void createExchange() {
        List<RabbitMQProperties.ExchangeConfig> exchanges = properties.getExchanges();
        if (CollectionUtils.isEmpty(exchanges)) return;
        exchanges.forEach(e -> {
            Exchange exchange = null;
            switch (e.getType()) {
                case DIRECT ->
                        exchange = new DirectExchange(e.getName(), e.getDurable(), e.getAutoDelete(), e.getArguments());
                case TOPIC ->
                        exchange = new TopicExchange(e.getName(), e.getDurable(), e.getAutoDelete(), e.getArguments());
                case HEADERS ->
                        exchange = new HeadersExchange(e.getName(), e.getDurable(), e.getAutoDelete(), e.getArguments());
                case FANOUT ->
                        exchange = new FanoutExchange(e.getName(), e.getDurable(), e.getAutoDelete(), e.getArguments());
                case CUSTOM ->
                        exchange = new CustomExchange(e.getName(), e.getCustomType(), e.getDurable(), e.getAutoDelete(), e.getArguments());
            }
            // 将交换机注册到spring bean工厂 让spring实现交换机的管理
            registerBean(e.getName(), exchange);
        });
    }

    /**
     * 动态绑定队列和交换机
     */
    public void bindingQueueToExchange() {
        List<RabbitMQProperties.QueueConfig> queues = properties.getQueues();
        if (CollectionUtils.isEmpty(queues)) return;
        queues.forEach(q -> {
            // 创建队列
            Queue queue = new Queue(q.getName(), q.getDurable(),
                    q.getExclusive(), q.getAutoDelete(), q.getArgs());
            // 注册队列bean
            registerBean(q.getName(), queue);
            // 注册绑定关系
            if (q.getExchangeNames() == null) return;
            List<String> exchangeNameList = Arrays.stream(q.getExchangeNames()).toList();
            exchangeNameList.forEach(name -> {
                // 获取交换机配置参数
                RabbitMQProperties.ExchangeConfig exchangeConfig = getExchangeConfig(name);
                bindingBuilder(queue, q, exchangeConfig);
            });

        });
    }

    public void bindingBuilder(Queue queue, RabbitMQProperties.QueueConfig q, RabbitMQProperties.ExchangeConfig exchangeConfig) {
        // 声明绑定关系
        Binding binding = null;
        // 根据不同的交换机模式 获取不同的交换机对象（注意：刚才注册时使用的是父类Exchange，这里获取的时候将类型获取成相应的子类）生成不同的绑定规则
        String exchangeName = exchangeConfig.getName();
        try {
            switch (exchangeConfig.getType()) {
                case TOPIC -> binding = BindingBuilder.bind(queue)
                        .to(getBean(exchangeName, TopicExchange.class))
                        .with(q.getRoutingKey());
                case DIRECT -> binding = BindingBuilder.bind(queue)
                        .to(getBean(exchangeName, DirectExchange.class))
                        .with(q.getRoutingKey());
                case HEADERS -> {
                    if (q.getWhereAll()) {
                        binding = BindingBuilder.bind(queue)
                                .to(getBean(exchangeName, HeadersExchange.class))
                                .whereAll(q.getHeaders()).match();
                    } else {
                        binding = BindingBuilder.bind(queue)
                                .to(getBean(exchangeName, HeadersExchange.class))
                                .whereAny(q.getHeaders()).match();
                    }
                }
                case FANOUT -> binding = BindingBuilder.bind(queue)
                        .to(getBean(exchangeName, FanoutExchange.class));
                case CUSTOM -> binding = BindingBuilder.bind(queue)
                        .to(getBean(exchangeName, CustomExchange.class))
                        .with(q.getRoutingKey()).noargs();
                default -> log.warn("queue [{}] config unspecified exchange!", q.getName());
            }
            registerBean(q.getName() + StringPool.DASHED + exchangeName, binding);
        } catch (BeansException e) {
            log.error("exchange [{}] no exist", exchangeName);
            throw e;
        }
    }

    /**
     * 将bean对象注册到bean工厂
     */
    public <T> void registerBean(String beanName, T bean) {
        if (Objects.isNull(bean)) return;
        // 将bean对象注册到bean工厂
        try {
            beanFactory.registerSingleton(beanName, bean);
        } catch (Exception e) {
            log.error("beanName [{}] is exist,please check config", beanName);
            throw e;
        }
    }

    /**
     * 根据beanName和class得到Bean
     */
    public <T> T getBean(String name, Class<T> clz) throws BeansException {
        return beanFactory.getBean(name, clz);
    }

}

