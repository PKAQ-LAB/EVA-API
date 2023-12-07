package tech.yunyue.core.log.base.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.yunyue.core.log.base.LogEntity;
import tech.yunyue.core.mvc.vo.PageBo;

import java.util.Date;

@Schema(description = "日志列表请求参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class LogQueryBo<T extends LogEntity> extends PageBo {

    @Schema(description = "开始时间")
    private Date begin;

    @Schema(description = "结束时间")
    private Date end;

    private T logEntity;

}
