package tech.yunyue.core.log.supporter.mongo.entity;

import lombok.Data;
import tech.yunyue.core.log.base.LoginlogEntity;
import tech.yunyue.core.log.base.ReportLogEntity;

import java.util.Date;

/**
 * 报表操作日志 mongo实体类
 */
@Data
public class MongoReportLogEntity extends ReportLogEntity {
    private String id;
    /**
     * mongo TTL索引使用
     **/
    private Date expireTime = new Date();
}
