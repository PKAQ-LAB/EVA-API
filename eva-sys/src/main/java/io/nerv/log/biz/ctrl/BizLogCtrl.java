package io.nerv.log.biz.ctrl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.nerv.core.bizlog.condition.MybatisSupporterCondition;
import io.nerv.core.bizlog.supporter.mybatis.entity.MybatisBizLogEntity;
import io.nerv.core.bizlog.supporter.mybatis.mapper.MybatisSupporterMapper;
import io.nerv.core.mvc.util.DateRangeVo;
import io.nerv.core.mvc.util.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@RestController
@RequestMapping("/monitor/log/biz")
@Api(tags = "业务日志")
@Conditional(MybatisSupporterCondition.class)
public class BizLogCtrl {
    @Autowired
    private MybatisSupporterMapper mybatisSupporterMapper;

    @GetMapping({"/get/{id}"})
    @ApiOperation(value = "根据id获取操作日志明细",response = Response.class)
    public Response query(@ApiParam(name = "id", value = "操作日志id")
                          @PathVariable(name = "id") String id){
        return new Response().success(this.mybatisSupporterMapper.selectById(id));
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取日志列表", response = Response.class)
    public Response list(@ApiParam(name ="dateRange", value = "日志对象") DateRangeVo dateRange,
                         @ApiParam(name ="pageNo", value = "页码") Integer pageNo,
                         @ApiParam(name ="pageCount", value = "条数") Integer pageCount){

        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();

        wrapper.ge("OPERATE_DATETIME", dateRange.getBegin());
        wrapper.le("OPERATE_DATETIME", dateRange.getEnd());

        Page pagination = new Page();
        pagination.setCurrent(pageNo == null? 1 : pageNo);
        pagination.setSize(pageCount == null? 10 : pageCount);

        return new Response().success(this.mybatisSupporterMapper.selectPage(pagination, wrapper));
    }

}
