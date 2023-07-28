package tech.yunyue.core.properties;

import lombok.Data;

/**
 * 业务日志配置读取类
 *
 * @author: S.PKAQ
 */
@Data
public class BizLog {
    /**
     * 实现类
     */
    private String impl = "";
    /**
     * 数据表名
     */
    private String dbName = "log_biz";
    /**
     * 历史数据表名
     */
    private String hisDBName = "history_log_biz";
    /**
     * 操作时间字段
     */
    private String dateField = "operate_datetime";

    //是否推送到mq
    private boolean mqEnabled = false;
    private Rabbit rabbit = new Rabbit();
    /**
     * 推送到rabbit的配置
     */
    @Data
    public static class Rabbit {
        private String exchange = "xmc.log";
        private String routingKey = "log";
    }
}
