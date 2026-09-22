package org.pkaq.core.mybatis.exception.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.log.base.ErrorIncident;

/**
 * PostgreSQL 错误事件实体。
 *
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Alias("errorIncident")
@TableName("log_error")
@Accessors(chain = true)
public class ErrorIncidentEntity extends ErrorIncident {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
}
