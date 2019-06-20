package io.nerv.log.ctrl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.nerv.core.bizlog.condition.MybatisSupporterCondition;
import io.nerv.core.bizlog.supporter.mybatis.entity.MybatisBizLogEntity;
import io.nerv.core.bizlog.supporter.mybatis.mapper.MybatisSupporterMapper;
import io.nerv.core.mvc.util.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
@Api(tags = "业务日志")
@Conditional(MybatisSupporterCondition.class)
public class BizLogCtrl {
    @Autowired
    private MybatisSupporterMapper mybatisSupporterMapper;

    @GetMapping("/list")
    @ApiOperation(value = "获取日志列表", response = Response.class)
    public Response list(@ApiParam(name ="condition", value = "日志对象") MybatisBizLogEntity entity,
                         @ApiParam(name ="pageNo", value = "页码") Integer pageNo,
                         @ApiParam(name ="pageCount", value = "条数") Integer pageCount){

        Wrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>(entity);

        Page pagination = new Page();
        pagination.setCurrent(pageNo);
        pagination.setSize(pageCount);

        return new Response().success(this.mybatisSupporterMapper.selectPage(pagination, wrapper));
    }

    @GetMapping("/listTop")
    @ApiOperation(value = "获取日志头X条", response = Response.class)
    public Response list(@ApiParam(name ="condition", value = "日志对象") MybatisBizLogEntity entity,
                         @ApiParam(name ="top", value = "条数") Integer top){

        QueryWrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>(entity);
        wrapper.last("limit " + top);

        return new Response().success(this.mybatisSupporterMapper.selectList(wrapper));
    }
}
