package tech.yunyue.core.log.config;

import tech.yunyue.core.log.base.*;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.log.events.ErrorLogEvent;
import tech.yunyue.core.log.events.LoginLogEvent;
import tech.yunyue.core.log.events.ReportLogEvent;

public interface LogConfig {
    // 都是通过@bean注入实现类 所以beanName即方法名
    // 业务日志Supporter的beanName
    String BIZ_LOG_NAME = "bizLogSupporter";
    // 错误日志errorLogSupporter的beanName
    String ERROR_LOG_NAME = "errorLogSupporter";
    // 登录/登出日志errorLogSupporter的beanName
    String LOGIN_LOG_NAME = "loginLogSupporter";
    // 报表日志errorLogSupporter的beanName
    String REPORT_LOG_NAME = "reportLogSupporter";

    <T extends BizLogEntity> LogSupporter<T, BizLogEvent> bizLogSupporter();

    <T extends ErrorlogEntity> LogSupporter<T, ErrorLogEvent> errorLogSupporter();

    <T extends LoginlogEntity> LogSupporter<T, LoginLogEvent> loginLogSupporter();

    <T extends ReportLogEntity> LogSupporter<T, ReportLogEvent> reportLogSupporter();
}
