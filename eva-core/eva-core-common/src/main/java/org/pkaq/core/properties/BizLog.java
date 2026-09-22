package org.pkaq.core.properties;

import lombok.Data;

/**
 * 业务日志配置读取类
 *
 * @author PKAQ
 */
@Data
public class BizLog {
    /**
     * 是否启用
     */
    private boolean enabled = false;
    /**
     * 业务日志归档配置。
     */
    private Archive archive = new Archive();

    /**
     * 业务日志归档配置。
     */
    @Data
    public static class Archive {
        /** 是否启用自动归档 */
        private boolean enabled = true;
        /** 热数据保留月数 */
        private int afterMonths = 6;
        /** 单批归档条数 */
        private int batchSize = 1000;
        /** 单次任务最大批次数 */
        private int maxBatches = 100;
        /** 归档任务表达式 */
        private String cron = "0 20 2 * * *";
        /** 归档任务时区 */
        private String zone = "Asia/Shanghai";
    }

}
