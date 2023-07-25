package tech.yunyue.core.mybatis.exception.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;
import tech.yunyue.core.log.base.ErrorlogEntity;

/**
 * 异常日志实体类
 */
@Data
@Alias("errorlog")
@TableName("log_error")
@EqualsAndHashCode(callSuper = true)
public class MybatisErrorlogEntity  extends ErrorlogEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
}
