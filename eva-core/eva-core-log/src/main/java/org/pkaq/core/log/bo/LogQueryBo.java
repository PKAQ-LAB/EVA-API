package org.pkaq.core.log.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.PageBo;

import java.time.LocalDateTime;

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
    private LocalDateTime begin;

    @Schema(description = "结束时间")
    private LocalDateTime end;
}
