package tech.yunyue.core.log.config;

import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.base.LogSupporter;
import tech.yunyue.core.log.base.LoginlogEntity;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.log.events.ErrorLogEvent;
import tech.yunyue.core.log.events.LoginLogEvent;

public interface LogConfig {
    // 都是通过@bean注入实现类 所以beanName即方法名
    // 业务日志Supporter的beanName
    String BIZ_LOG_NAME = "bizLogSupporter";
    // 错误日志errorLogSupporter的beanName
    String ERROR_LOG_NAME = "errorLogSupporter";
    // 登录/登出日志errorLogSupporter的beanName
    String LOGIN_LOG_NAME = "loginLogSupporter";
    <T extends BizLogEntity> LogSupporter<T, BizLogEvent> bizLogSupporter();

    <T extends ErrorlogEntity> LogSupporter<T, ErrorLogEvent> errorLogSupporter();

    <T extends LoginlogEntity> LogSupporter<T, LoginLogEvent> loginLogSupporter();
}
