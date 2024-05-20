package tech.yunyue.core.mybatis.log.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import tech.yunyue.core.log.base.ReportLogEntity;

/**
 * 报表日志 mybatis实体类
 */
@Data
@Alias("logreport")
@TableName("log_report")
@EqualsAndHashCode(callSuper = true)
public class MybatisReportLogEntity extends ReportLogEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
}
