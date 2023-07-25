package tech.yunyue.core.properties;

import lombok.Data;

/**
 * 错误日志配置读取类
 *
 * @author: S.PKAQ
 */
@Data
public class ErrorLog {
    /**
     * 是否启用
     */
    private boolean enabled = false;
    /**
     * 实现类
     */
    private String impl = "";
    /**
     * 是否推送到mq
     */
    private boolean mqEnabled = false;
    private Rabbit rabbit = new Rabbit();
    /**
     * 推送到mq的配置
     */
    @Data
    public static class Rabbit {
        private String bizExchange = "xmc.errorlog";
        private String bizRoutingKey = "errorlog";
    }
}
