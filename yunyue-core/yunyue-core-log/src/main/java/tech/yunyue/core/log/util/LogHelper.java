package tech.yunyue.core.log.util;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.base.LogSupporter;
import tech.yunyue.core.log.base.LoginlogEntity;
import tech.yunyue.core.log.config.LogConfig;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.log.events.ErrorLogEvent;
import tech.yunyue.core.log.events.LoginLogEvent;
import tech.yunyue.core.threaduser.ThreadUserHelper;

@Component
public class LogHelper {
    private final ApplicationEventPublisher eventPublisher;
    private final LogSupporter bizLogSupporter;
    private final LogSupporter errorLogSupporter;
    private final LogSupporter loginLogSupporter;

    private final LogSupporter reportLogSupporter;

    public LogHelper(ApplicationEventPublisher eventPublisher,
                     @Qualifier(LogConfig.BIZ_LOG_NAME) LogSupporter bizLogSupporter,
                     @Qualifier(LogConfig.ERROR_LOG_NAME) LogSupporter errorLogSupporter,
                     @Qualifier(LogConfig.LOGIN_LOG_NAME) LogSupporter loginLogSupporter,
                     @Qualifier(LogConfig.REPORT_LOG_NAME) LogSupporter reportLogSupporter) {
        this.eventPublisher = eventPublisher;
        this.bizLogSupporter = bizLogSupporter;
        this.errorLogSupporter = errorLogSupporter;
        this.loginLogSupporter = loginLogSupporter;
        this.reportLogSupporter = reportLogSupporter;
    }

    public void save(BizLogEntity bizLogEntity) {
        bizLogEntity.setMCode(ThreadUserHelper.getMcode())
                .setDevice(ThreadUserHelper.getDevice())
                .setVersion(ThreadUserHelper.getVersion())
                .setCreateId(ThreadUserHelper.getUserId())
                .setPostId(ThreadUserHelper.getPostId())
                .setOrgId(ThreadUserHelper.getOrgId())
                .setTenantId(ThreadUserHelper.getTenantId());
        eventPublisher.publishEvent(new BizLogEvent(bizLogEntity));
    }

    public void save(ErrorlogEntity errorlogEntity) {
        eventPublisher.publishEvent(new ErrorLogEvent(errorlogEntity));
    }

    public void save(LoginlogEntity loginlogEntity) {
        eventPublisher.publishEvent(new LoginLogEvent(loginlogEntity));
    }

    public LogSupporter getBizLogSupporter() {
        return bizLogSupporter;
    }

    public LogSupporter getErrorLogSupporter() {
        return errorLogSupporter;
    }

    public LogSupporter getLoginLogSupporter() {
        return loginLogSupporter;
    }

    public LogSupporter getReportLogSupporter() {
        return reportLogSupporter;
    }

}
