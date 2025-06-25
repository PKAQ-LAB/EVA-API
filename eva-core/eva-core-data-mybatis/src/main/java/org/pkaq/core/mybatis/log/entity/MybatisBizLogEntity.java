package org.pkaq.core.mybatis.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.log.base.BizLogEntity;

/**
 * 业务日志 mybatis实体类
 *
 * @author: S.PKAQ
 */
@Data
@Alias("bizlog")
@TableName("log_biz")
@EqualsAndHashCode(callSuper = true)
public class MybatisBizLogEntity extends BizLogEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
}
