package org.pkaq.core.log.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.AbstractDiscriminator;
import org.pkaq.core.tenant.TenantLogContext;

/**
 * 将日志按可信租户和类型分流，路径不接受请求参数或任意字符串。
 *
 * @author PKAQ
 * @date 2026-11-11
 */
public class TenantLogDiscriminator extends AbstractDiscriminator<ILoggingEvent> {
    /** 是否启用租户文件隔离。 */
    private boolean tenantEnabled;

    /**
     * 由日志配置注入租户开关。
     */
    public void setTenantEnabled(boolean enabled) {
        tenantEnabled = enabled;
    }

    /**
     * 返回可用于文件路径的有限日志类型和规范数字租户标识。
     */
    @Override
    public String getDiscriminatingValue(ILoggingEvent event) {
        String tenant = event.getMDCPropertyMap().get(TenantLogContext.KEY);
        // 同步运行日志从服务端上下文取值，异步事件优先使用提交时快照。
        if (tenant == null) {
            tenant = TenantLogContext.getTenantId();
        }
        String directory = "standalone";
        // 未知或平台身份集中到平台目录，非法值不得进入文件路径。
        if (tenantEnabled) {
            directory = tenant != null && tenant.matches("[1-9][0-9]{0,18}")
                    ? "tenant-" + tenant : "platform";
        }
        String logger = event.getLoggerName();
        String type = "application";
        // 审计类型独立分流，普通 ERROR 及完整堆栈写入错误文件。
        if ("eva.audit.operation".equals(logger)) {
            type = "operation";
        } else if ("eva.audit.login".equals(logger)) {
            type = "login";
        } else if ("eva.audit.error".equals(logger) || event.getLevel().isGreaterOrEqual(Level.ERROR)) {
            type = "error";
        }
        return directory + "/" + type;
    }

    /**
     * 提供分流结果的日志配置变量名。
     */
    @Override
    public String getKey() {
        return "evaLogRoute";
    }
}
