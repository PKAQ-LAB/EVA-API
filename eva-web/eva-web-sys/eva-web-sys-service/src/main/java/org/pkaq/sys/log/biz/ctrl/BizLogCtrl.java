package org.pkaq.sys.log.biz.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.log.base.LogSupporter;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 业务日志控制器
 *
 * @author PKAQ
 */
@RestController
@RequestMapping("/monitor/log/biz")
@Tag(name = "业务日志")
@RequiredArgsConstructor
public class BizLogCtrl extends Ctrl {
    private final LogSupporter logSupporter;

    /**
     * 根据id获取操作日志明细
     */
    @GetMapping({"/get/{id}"})
    @Operation(description = "根据id获取操作日志明细")
    public Response<Object> query(@Parameter(name = "id", description = "操作日志id")
                                  @PathVariable(name = "id") String id) {
        return success(this.logSupporter.get(id));
    }

    /**
     * 获取日志列表
     */
    @GetMapping("/list")
    @Operation(description = "获取日志列表")
    public Response<Object> list(@Parameter(name = "dateRange", description = "查询区间") DateRangeBo dateRange) {
        return Response.success(this.logSupporter.list(dateRange));
    }
}
