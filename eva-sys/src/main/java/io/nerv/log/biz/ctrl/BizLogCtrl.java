package io.nerv.log.biz.ctrl;

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
    public Response list(@ApiParam(name ="dateRange", value = "查询区间") DateRangeVo dateRange,
                         @ApiParam(name ="pageNo", value = "页码") Integer pageNo,
                         @ApiParam(name ="pageCount", value = "条数") Integer size){

        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>();

        LocalDate begin = dateRange.getBegin();
        LocalDate end = LocalDate.now();

        if (null == begin){
            begin = LocalDate.now().minusDays(7);
        }
        if (null == end){
            end = LocalDate.now();
        }
        wrapper.ge("OPERATE_DATETIME", begin);
        wrapper.le("OPERATE_DATETIME", end);

        Page pagination = new Page();
        pagination.setCurrent(pageNo == null? 1 : pageNo);
        pagination.setSize(size == null? 10 : size);

        return new Response().success(this.mybatisSupporterMapper.selectPage(pagination, wrapper));
    }

}
