package tech.yunyue.core.log.base.bo;

import lombok.Data;
import tech.yunyue.core.log.annotation.HistoryLog;
import tech.yunyue.core.log.supporter.mongo.entity.MongoReportLogEntity;

/**
 * @Author: mja
 * @CreateTime: 2024-07-30
 * @Description: 报表的打印次数mongo查询bo类。只是标识查询的时候打印次数表
 * @Version: 1.0
 */
@Data
@HistoryLog("REPORT_COUNT_LOG")
public class ReportPrintCountQueryBo extends LogQueryBo<MongoReportLogEntity> {

}
