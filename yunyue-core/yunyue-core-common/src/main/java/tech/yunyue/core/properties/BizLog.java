package tech.yunyue.core.properties;

import lombok.Data;

import java.util.List;

/**
 * 业务日志配置读取类
 *
 * @author: S.PKAQ
 */
@Data
public class BizLog {
    /**
     * 是否启用
     */
    private boolean enabled = false;
    /**
     * 实现类
     */
    private String impl = "";
    /**
     * 需要记录数据修改快照的业务表名
     * @return
     */
    private List<String> snapshotTableNames;

    private Rabbit rabbit = new Rabbit();
    /**
     * 推送到rabbit的配置
     */
    @Data
    public static class Rabbit {
        private String binExchange = "maxwell";
        private String bizExchange = "xmc.log";
        private String bizRoutingKey = "log";
    }
}
