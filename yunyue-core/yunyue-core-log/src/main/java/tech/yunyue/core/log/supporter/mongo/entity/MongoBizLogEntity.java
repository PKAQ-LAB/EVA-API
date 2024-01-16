package tech.yunyue.core.log.supporter.mongo.entity;

import lombok.Data;
import tech.yunyue.core.log.base.BizLogEntity;

import java.util.Date;

/**
 * 业务日志 mybatis实体类
 *
 * @author: S.PKAQ
 */
@Data
public class MongoBizLogEntity extends BizLogEntity {
    private String id;
    /**
     * mongo TTL索引使用
     **/
    private Date expireTime = new Date();
}
