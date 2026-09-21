package org.pkaq.core.log.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.PageBo;

import java.util.Date;

/**
 * 日志列表请求参数
 *
 * @author PKAQ
 */
@Schema(description = "日志列表请求参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class LogQueryBo extends PageBo {

    @Schema(description = "开始时间")
    private Date begin;

    @Schema(description = "结束时间")
    private Date end;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "操作类型")
    private String operateType;

    @Schema(description = "模块编码")
    private String mCode;

    @Schema(description = "业务ID")
    private String bId;

    @Schema(description = "是否查询归档数据")
    private Boolean includeArchived = Boolean.TRUE;

    @Schema(description = "平台管理员目标租户ID")
    private Long targetTenantId;
}
