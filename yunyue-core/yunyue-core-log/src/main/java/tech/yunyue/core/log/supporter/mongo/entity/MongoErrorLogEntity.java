package tech.yunyue.core.log.supporter.mongo.entity;

import lombok.Data;
import tech.yunyue.core.log.base.ErrorlogEntity;

import java.util.Date;

/**
 * 错误日志 mongo实体类
 */
@Data
public class MongoErrorLogEntity extends ErrorlogEntity {
    /**
     * mongo TTL索引使用
     **/
    private Date expireTime = new Date();
}
