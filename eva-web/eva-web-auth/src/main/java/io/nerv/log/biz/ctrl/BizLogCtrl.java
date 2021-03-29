package io.nerv.log.biz.ctrl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.nerv.core.bizlog.condition.MybatisSupporterCondition;
import io.nerv.core.bizlog.supporter.mybatis.entity.MybatisBizLogEntity;
import io.nerv.core.bizlog.supporter.mybatis.mapper.MybatisSupporterMapper;
import io.nerv.core.mvc.util.Page;
import io.nerv.core.mvc.vo.DateRangeVo;
import io.nerv.core.mvc.vo.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@RestController
@RequestMapping("/monitor/log/biz")
@Api(tags = "业务日志")
@Conditional(MybatisSupporterCondition.class)
@RequiredArgsConstructor
public class BizLogCtrl {
    private final MybatisSupporterMapper mybatisSupporterMapper;

    private final DatabaseIdProvider databaseIdProvider;

    private final DataSource dataSource;

    @GetMapping({"/get/{id}"})
    @ApiOperation(value = "根据id获取操作日志明细",response = Response.class)
    public Response query(@ApiParam(name = "id", value = "操作日志id")
                          @PathVariable(name = "id") String id){
        return new Response().success(this.mybatisSupporterMapper.selectById(id));
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取日志列表", response = Response.class)
    public Response list(@ApiParam(name ="dateRange", value = "查询区间") DateRangeVo dateRange,
                         @ApiParam(name ="pageNo", value = "页码") Integer pageNo,
                         @ApiParam(name ="pageCount", value = "条数") Integer size) throws SQLException {

        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();

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
            wrapper.ge("OPERATE_DATETIME", DateUtil.format(begin, DatePattern.NORM_DATE_PATTERN));
            wrapper.le("OPERATE_DATETIME", DateUtil.format(end, DatePattern.NORM_DATE_PATTERN));
        } else if ("mysql".equalsIgnoreCase(dbType)) {
            wrapper.ge("OPERATE_DATETIME", begin);
            wrapper.le("OPERATE_DATETIME", end);
        }

        Page pagination = new Page();
        pagination.setCurrent(pageNo == null? 1 : pageNo);
        pagination.setSize(size == null? 10 : size);

        return new Response().success(this.mybatisSupporterMapper.selectPage(pagination, wrapper));
    }

}
