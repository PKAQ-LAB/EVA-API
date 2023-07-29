package tech.yunyue.core.log.config;

import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.base.LogSupporter;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.log.events.ErrorLogEvent;

public interface LogConfig {
    // 都是通过@bean注入实现类 所以beanName即方法名
    // 业务日志Supporter的beanName
    String BIZ_LOG_NAME = "bizLogSupporter";
    // 错误日志errorLogSupporter的beanName
    String ERROR_LOG_NAME = "errorLogSupporter";
    <T extends BizLogEntity> LogSupporter<T, BizLogEvent> bizLogSupporter();

    <T extends ErrorlogEntity> LogSupporter<T, ErrorLogEvent> errorLogSupporter();
}
