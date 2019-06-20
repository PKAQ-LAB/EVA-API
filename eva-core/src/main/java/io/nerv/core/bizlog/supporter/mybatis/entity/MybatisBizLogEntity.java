package io.nerv.core.bizlog.supporter.mybatis.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.nerv.core.bizlog.base.BizLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

/**
 * 业务日志 mybatis实体类
 * @author: S.PKAQ
 * @Datetime: 2018/9/27 8:42
 */
@Data
@Alias("bizlog")
@TableName("log_biz")
@EqualsAndHashCode(callSuper = true)
public class MybatisBizLogEntity extends BizLogEntity {
    @TableId
    private String id;
}
