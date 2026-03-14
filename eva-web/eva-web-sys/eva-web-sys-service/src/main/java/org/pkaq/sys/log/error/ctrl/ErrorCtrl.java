package org.pkaq.sys.log.error.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.log.base.ErrorLogSupporter;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.util.DateUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author PKAQ
 */
@RestController
@RequestMapping("/monitor/log/error")
@Tag(name = "错误日志")
@RequiredArgsConstructor
public class ErrorCtrl extends Ctrl {

    private final ErrorLogSupporter errorLogSupporter;

    @GetMapping({"/get/{id}"})
    @Operation(summary = "根据id获取错误日志明细")
    public Response<Object> query(@Parameter(name = "id", description = "错误日志id")
                                  @PathVariable(name = "id") String id) {
        return Response.success(this.errorLogSupporter.get(id));
    }

    @GetMapping("/list")
    @Operation(summary = "获取错误日志列表")
    public Response<?> list(@Parameter(name = "dateRange", description = "查询区间") DateRangeBo dateRange,
                            @Parameter(name = "pageNo", description = "页码") Integer pageNo,
                            @Parameter(name = "pageCount", description = "条数") Integer pageCount) {

        if (dateRange.getBegin() == null) {
            dateRange.setBegin(DateUtils.addDay(new Date(), -7));
        }
        if (dateRange.getEnd() == null) {
            dateRange.setEnd(new Date());
        }

        return Response.success(this.errorLogSupporter.list(
                dateRange,
                pageNo == null ? 1 : pageNo,
                pageCount == null ? 10 : pageCount
        ));
    }
}
