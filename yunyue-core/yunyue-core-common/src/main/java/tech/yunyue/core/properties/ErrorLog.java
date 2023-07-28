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
     * 实现类
     */
    private String impl = "";
    /**
     * 数据表名
     */
    private String dbName = "log_error";
    /**
     * 历史数据表名
     */
    private String hisDBName;
    /**
     * 操作时间字段
     */
    private String dateField = "request_time";
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
        private String exchange = "xmc.errorlog";
        private String routingKey = "errorlog";
    }
}
