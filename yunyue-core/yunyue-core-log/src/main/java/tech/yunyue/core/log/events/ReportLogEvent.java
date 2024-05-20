package tech.yunyue.core.log.events;

import tech.yunyue.core.log.base.ReportLogEntity;
import tech.yunyue.core.log.supporter.mongo.entity.MongoReportLogEntity;

public class ReportLogEvent extends LogEvent<ReportLogEntity> {
    public ReportLogEvent(MongoReportLogEntity entity) {
        super(entity);
    }
}
