package io.nerv.log.error.ctrl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.nerv.core.bizlog.supporter.mybatis.mapper.MybatisSupporterMapper;
import io.nerv.core.exception.entity.ErrorlogEntity;
import io.nerv.core.exception.mapper.ErrorlogMapper;
import io.nerv.core.mvc.util.Page;
import io.nerv.core.mvc.vo.DateRangeVo;
import io.nerv.core.mvc.vo.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@RestController
@RequestMapping("/monitor/log/error")
@Api(tags = "错误日志")
@RequiredArgsConstructor
public class ErrorCtrl {

    private final ErrorlogMapper errorlogMapper;

    private final DatabaseIdProvider databaseIdProvider;

    private final DataSource dataSource;

    @GetMapping({"/get/{id}"})
    @ApiOperation(value = "根据id获取操作日志明细",response = Response.class)
    public Response query(@ApiParam(name = "id", value = "操作日志id")
                          @PathVariable(name = "id") String id){
        return new Response().success(this.errorlogMapper.selectById(id));
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取错误日志列表", response = Response.class)
    public Response list(@ApiParam(name ="dateRange", value = "查询区间") DateRangeVo dateRange,
                         @ApiParam(name ="pageNo", value = "页码") Integer pageNo,
                         @ApiParam(name ="pageCount", value = "条数") Integer pageCount) throws SQLException {

        QueryWrapper<ErrorlogEntity> wrapper = new QueryWrapper<>();

        Date begin = dateRange.getBegin();
        Date end = dateRange.getEnd();

        if (null == begin){
            begin = DateUtil.offsetDay(new Date(), -7);
        }
        if (null == end){
            end = new Date();
        }

        String dbType = databaseIdProvider.getDatabaseId(dataSource);

        if ("oracle".equalsIgnoreCase(dbType)){
            wrapper.ge("REQUEST_TIME", DateUtil.format(begin, DatePattern.NORM_DATE_PATTERN));
            wrapper.le("REQUEST_TIME", DateUtil.format(end, DatePattern.NORM_DATE_PATTERN));
        } else if ("mysql".equalsIgnoreCase(dbType)) {
            wrapper.ge("REQUEST_TIME", begin);
            wrapper.le("REQUEST_TIME", end);
        }

        Page pagination = new Page();
        pagination.setCurrent(pageNo == null? 1 : pageNo);
        pagination.setSize(pageCount == null? 10 : pageCount);

        return new Response().success(this.errorlogMapper.selectPage(pagination, wrapper));
    }

}
