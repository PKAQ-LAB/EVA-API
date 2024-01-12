package tech.yunyue.core.rabbitmq.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * rabbitmq 创建消息队列和交换机的配置文件
 */
@ConfigurationProperties(prefix = "eva.rabbit")
@Data
public class RabbitMQProperties {
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

}

