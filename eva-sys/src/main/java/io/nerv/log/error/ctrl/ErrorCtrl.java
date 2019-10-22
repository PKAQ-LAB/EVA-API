package io.nerv.log.error.ctrl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.nerv.core.bizlog.supporter.mybatis.entity.MybatisBizLogEntity;
import io.nerv.core.bizlog.supporter.mybatis.mapper.MybatisSupporterMapper;
import io.nerv.core.mvc.util.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitor/log/error")
@Api(tags = "错误日志")
public class ErrorCtrl {
    @Autowired
    private MybatisSupporterMapper mybatisSupporterMapper;

    @GetMapping("/list")
    @ApiOperation(value = "获取错误日志列表", response = Response.class)
    public Response list(@ApiParam(name ="condition", value = "日志对象") MybatisBizLogEntity entity,
                         @ApiParam(name ="pageNo", value = "页码") Integer pageNo,
                         @ApiParam(name ="pageCount", value = "条数") Integer pageCount){

        Wrapper<MybatisBizLogEntity> wrapper = new QueryWrapper<>(entity);

        Page pagination = new Page();
        pagination.setCurrent(pageNo == null? 1 : pageNo);
        pagination.setSize(pageCount == null? 10 : pageCount);

        return new Response().success(this.mybatisSupporterMapper.selectPage(pagination, wrapper));
    }

}
