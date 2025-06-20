package org.pkaq.sys.log.biz.ctrl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.pkaq.core.log.base.BizLogSupporter;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.mybatis.log.entity.MybatisBizLogEntity;
import org.pkaq.core.mybatis.util.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author PKAQ
 */
@RestController
@RequestMapping("/monitor/log/biz")
@Tag(name = "业务日志")
@RequiredArgsConstructor
public class BizLogCtrl extends Ctrl {
    private final BizLogSupporter bizLogSupporter;

    private final DatabaseIdProvider databaseIdProvider;

    private final DataSource dataSource;

    @GetMapping({"/get/{id}"})
    @Operation(description = "根据id获取操作日志明细")
    public Response<Object> query(@Parameter(name = "id", description = "操作日志id")
                          @PathVariable(name = "id") String id) {
        return success(this.bizLogSupporter.get(id));
    }

    @GetMapping("/list")
    @Operation(description = "获取日志列表")
    public Response<Object> list(@Parameter(name = "dateRange", description = "查询区间") DateRangeBo dateRange,
                         @Parameter(name = "pageNo", description = "页码") Integer pageNo,
                         @Parameter(name = "pageCount", description = "条数") Integer size) throws SQLException {

        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();

        Date begin = dateRange.getBegin();
        Date end = dateRange.getEnd();

        if (null == begin) {
            begin = DateUtil.offsetDay(new Date(), -7);
        }
        if (null == end) {
            end = new Date();
        }

        String dbType = databaseIdProvider.getDatabaseId(dataSource);

        if ("oracle".equalsIgnoreCase(dbType)) {
            wrapper.ge("OPERATE_DATETIME", DateUtil.format(begin, DatePattern.NORM_DATE_PATTERN));
            wrapper.le("OPERATE_DATETIME", DateUtil.format(end, DatePattern.NORM_DATE_PATTERN));
        } else if ("mysql".equalsIgnoreCase(dbType)) {
            wrapper.ge("OPERATE_DATETIME", begin);
            wrapper.le("OPERATE_DATETIME", end);
        }

        wrapper.orderByDesc("OPERATE_DATETIME");

        Page pagination = new Page();
        pagination.setCurrent(pageNo == null ? 1 : pageNo);
        pagination.setSize(size == null ? 30 : size);

        return new Response().success(this.bizLogSupporter.list(dateRange));
    }

}
