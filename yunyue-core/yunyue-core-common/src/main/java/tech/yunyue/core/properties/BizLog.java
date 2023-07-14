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
     * 是否启用
     */
    private boolean enabled = false;
    /**
     * 实现类
     */
    private String impl = "";

    private Rabbit rabbit;
    /**
     * 推送到rabbit的配置
     */
    @Data
    public static class Rabbit {
        private String exchange;
        private String routingKey;
    }
}
