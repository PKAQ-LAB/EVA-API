package tech.yunyue.core.log.events;

import tech.yunyue.core.log.base.ReportLogEntity;

public class ReportLogEvent extends LogEvent<ReportLogEntity> {
    public ReportLogEvent(ReportLogEntity entity) {
        super(entity);
    }
}
