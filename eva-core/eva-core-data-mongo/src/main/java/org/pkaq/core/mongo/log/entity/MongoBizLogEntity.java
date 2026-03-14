package org.pkaq.core.mongo.log.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.log.base.BizLogEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB业务日志实体类
 *
 * @author PKAQ
 * @date 2026-03-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "log_biz")
public class MongoBizLogEntity extends BizLogEntity {

    @Id
    private String id;
}
