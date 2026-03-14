package org.pkaq.sys.log.biz.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.log.base.LogSupporter;
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
     *
     * @param id 操作日志id
     * @return 日志详情
     */
    @GetMapping({"/get/{id}"})
    @Operation(description = "根据id获取操作日志明细")
    public Response<Object> query(@Parameter(name = "id", description = "操作日志id")
                                  @PathVariable(name = "id") String id) {
        return success(this.logSupporter.get(id));
    }

    /**
     * 获取日志列表
     * 日期范围为空时默认查询最近7天，按操作时间降序排列
     *
     * @param dateRange 查询区间
     * @param pageNo    页码
     * @param size      每页条数
     * @return 分页日志列表
     */
    @GetMapping("/list")
    @Operation(description = "获取日志列表")
    public Response<Object> list(@Parameter(name = "dateRange", description = "查询区间") DateRangeBo dateRange,
                                 @Parameter(name = "pageNo", description = "页码") Integer pageNo,
                                 @Parameter(name = "pageCount", description = "条数") Integer size) {
        // 默认查询最近7天
        if (null == dateRange.getBegin()) {
            dateRange.setBegin(DateUtils.addDay(new Date(), -7));
        }
        if (null == dateRange.getEnd()) {
            dateRange.setEnd(new Date());
        }

        // 分页参数默认值
        if (null == pageNo || pageNo < 1) {
            pageNo = 1;
        }
        if (null == size || size < 1) {
            size = 30;
        }

        return Response.success(this.logSupporter.list(dateRange, pageNo, size));
    }
}
