package tech.yunyue.core.log.base.bo;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.yunyue.core.log.base.LogEntity;
import tech.yunyue.core.mvc.vo.PageBo;

import java.util.Date;
import java.util.Optional;

@Schema(description = "日志列表请求参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class LogQueryBo<T extends LogEntity> extends PageBo {

    @Schema(description = "开始时间")
    private DateTime begin;

    @Schema(description = "结束时间")
    private DateTime end;

    private T logEntity;

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public void setBegin(DateTime begin) {
        this.begin = begin;
    }

    public void setEnd(Date end) {
        Optional.ofNullable(end).ifPresent(e -> {
            this.end = DateUtil.endOfDay(end);
        });
    }

    public void setBegin(Date begin) {
        Optional.ofNullable(begin).ifPresent(e -> {
            this.begin = DateUtil.beginOfDay(begin);
        });
    }
}
