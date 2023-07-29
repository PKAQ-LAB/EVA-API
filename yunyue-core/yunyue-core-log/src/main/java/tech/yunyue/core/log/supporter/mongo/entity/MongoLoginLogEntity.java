package tech.yunyue.core.log.supporter.mongo.entity;

import lombok.Data;
import tech.yunyue.core.log.base.LoginlogEntity;

import java.util.Date;

/**
 * 登录/登出日志 mongo实体类
 */
@Data
public class MongoLoginLogEntity extends LoginlogEntity {
    /**
     * mongo TTL索引使用
     **/
    private Date expireTime = new Date();
}
