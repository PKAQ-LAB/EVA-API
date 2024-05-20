package tech.yunyue.core.log.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 报表操作日志 mongo实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ReportLogEntity extends LogEntity {
    /**
     * 报表id
     **/
    @Schema(description = "报表id")
    private String reportId;
    /**
     * 报表编码
     **/
    @Schema(description = "报表编码")
    private String reportCode;

    /**
     * 报表名称
     **/
    @Schema(description = "报表名称")
    private String reportName;

    /**
     * 操作类型
     **/
    @Schema(description = "操作类型")
    private String operateType;

    /**
     * 操作时间
     **/
    @Schema(description = "操作时间")
    private String operateDatetime;

    /**
     * 操作描述
     **/
    @Schema(description = "操作描述")
    private String description;
}
