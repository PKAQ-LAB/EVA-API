package tech.yunyue.core.properties;

import lombok.Data;

/**
 * 登录/登出日志配置读取类
 */
@Data
public class LoginLog {
    /**
     * 实现类
     */
    private String impl = "";
    /**
     * 数据表名
     */
    private String dbName = "log_login";
    /**
     * 历史数据表名
     */
    private String hisDBName;
    /**
     * 操作时间字段
     */
    private String dateField = "operate_datetime";
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
        private String exchange = "xmc.log";
        private String routingKey = "login";
    }
}
