package tech.yunyue.core.rabbitmq.config;


import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import tech.yunyue.core.util.StringPool;

import java.util.*;

/**
 * rabbitmq 消息队列和交换机 配置文件
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "eva.rabbit")
@EnableConfigurationProperties
public class RabbitMQProperties {
    /**
     * Spring应用上下文环境
     */
    @Autowired
    private ConfigurableListableBeanFactory beanFactory;
    /**
     * 装载自定义配置交换机
     */
    private List<ExchangeConfig> exchanges = new ArrayList<>();
    /**
     * 装载自定义配置队列
     */
    private List<QueueConfig> queues = new ArrayList<>();

    @Data
    public static class ExchangeConfig {
        /**
         * 交换机名
         */
        private String name;
        /**
         * 交换机类型
         */
        private ExchangeType type;
        /**
         * 是否为持久交换机（该交换机将在服务器重启后保留下来）
         */
        private Boolean durable = Boolean.FALSE;
        /**
         * 服务器在不再使用该交换机时将其删除
         */
        private Boolean autoDelete = Boolean.FALSE;
        /**
         * 自定义交换机类型
         */
        private String customType;
        /**
         * 交换机参数（自定义交换机）
         */
        private Map<String, Object> arguments;

    }
    @Data
    public static class QueueConfig {

        /**
         * 队列名（每个队列的名称应该唯一）
         * 必须*
         */
        private String name;

        /**
         * 指定绑定交互机，可绑定多个（逗号分隔）
         * 必须*
         */
        private String[] exchangeNames;

        /**
         * 队列路由键
         */
        private String routingKey;

        /**
         * 是否为持久队列（该队列将在服务器重启后保留下来）
         */
        private Boolean durable = Boolean.FALSE;

        /**
         * 是否为排它队列
         */
        private Boolean exclusive = Boolean.FALSE;

        /**
         * 如果队列为空是否删除（如果服务器在不使用队列时是否删除队列）
         */
        private Boolean autoDelete = Boolean.FALSE;

        /**
         * 头队列是否全部匹配
         * 默认：是
         */
        private Boolean whereAll = Boolean.TRUE;

        /**
         * 参数
         */
        private Map<String, Object> args;

        /**
         * 消息头
         */
        private Map<String, Object> headers;

    }
    public enum ExchangeType {
        /**
         * 自定义交换机
         */
        CUSTOM,
        /**
         * 直连交换机（全文匹配）
         */
        DIRECT,
        /**
         * 通配符交换机（两种通配符：*只能匹配一个单词，#可以匹配零个或多个）
         */
        TOPIC,
        /**
         * 头交换机（自定义键值对匹配，根据发送消息内容中的headers属性进行匹配）
         */
        HEADERS,
        /**
         * 扇形（广播）交换机 （将消息转发到所有与该交互机绑定的队列上）
         */
        FANOUT;
    }

    public ExchangeConfig getExchangeConfig(String name) {
        return exchanges.stream().filter(e->e.getName().equals(name)).findFirst().orElse(null);
    }

    @PostConstruct
    public void init() {
        // 创建交换机
        createExchange();
        // 绑定队列和交换机
        bindingQueueToExchange();
    }

    /**
     * 动态创建交换机
     */
    public void createExchange() {
        List<ExchangeConfig> exchanges = getExchanges();
        if (CollectionUtils.isEmpty(exchanges)) return;
        exchanges.forEach(e -> {
            Exchange exchange = null;
            switch (e.getType()) {
                case DIRECT -> exchange = new DirectExchange(e.getName(), e.getDurable(), e.getAutoDelete(), e.getArguments());
                case TOPIC -> exchange = new TopicExchange(e.getName(), e.getDurable(), e.getAutoDelete(), e.getArguments());
                case HEADERS -> exchange = new HeadersExchange(e.getName(), e.getDurable(), e.getAutoDelete(), e.getArguments());
                case FANOUT -> exchange = new FanoutExchange(e.getName(), e.getDurable(), e.getAutoDelete(), e.getArguments());
                case CUSTOM -> exchange = new CustomExchange(e.getName(), e.getCustomType(), e.getDurable(), e.getAutoDelete(), e.getArguments());
            }
            // 将交换机注册到spring bean工厂 让spring实现交换机的管理
            registerBean(e.getName(), exchange);
        });
    }

    /**
     * 动态绑定队列和交换机
     */
    public void bindingQueueToExchange() {
        List<QueueConfig> queues = getQueues();
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
                ExchangeConfig exchangeConfig = getExchangeConfig(name);
                bindingBuilder(queue, q, exchangeConfig);
            });

        });
    }

    public void bindingBuilder(Queue queue, QueueConfig q, ExchangeConfig exchangeConfig) {
        // 声明绑定关系
        Binding binding = null;
        // 根据不同的交换机模式 获取不同的交换机对象（注意：刚才注册时使用的是父类Exchange，这里获取的时候将类型获取成相应的子类）生成不同的绑定规则
        String exchangeName = exchangeConfig.getName();
        try {
            switch (exchangeConfig.getType()) {
                case TOPIC ->
                        binding = BindingBuilder.bind(queue)
                                .to(getBean(exchangeName, TopicExchange.class))
                                .with(q.getRoutingKey());
                case DIRECT ->
                        binding = BindingBuilder.bind(queue)
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
                case FANOUT ->
                        binding = BindingBuilder.bind(queue)
                                .to(getBean(exchangeName, FanoutExchange.class));
                case CUSTOM ->
                        binding = BindingBuilder.bind(queue)
                                .to(getBean(exchangeName, CustomExchange.class))
                                .with(q.getRoutingKey()).noargs();
                default ->
                        log.warn("queue [{}] config unspecified exchange!", q.getName());
            }
            registerBean(q.getName() + StringPool.DASHED + exchangeName, binding);
        }catch (BeansException e) {
            log.error("exchange [{}] no exist", exchangeName);
            throw e;
        }
    }

    /**
     * 将bean对象注册到bean工厂
     */
    public <T> void registerBean(String beanName, T bean) {
        if(Objects.isNull(bean)) return;
        // 将bean对象注册到bean工厂
        try {
            beanFactory.registerSingleton(beanName, bean);
        }catch (Exception e) {
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

