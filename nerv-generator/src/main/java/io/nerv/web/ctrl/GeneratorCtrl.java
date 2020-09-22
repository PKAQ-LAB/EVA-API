package io.nerv.web.ctrl;

import io.nerv.core.mvc.vo.Response;
import io.nerv.web.entity.GeneratorConfig;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/generator")
public class GeneratorCtrl {

    @GetMapping("/loadTableStructure")
    @ApiOperation(value = "加载表结构",response = Response.class)
    public Response loadTableStructure(@ApiParam(name ="tableName", value = "表名")
                                       String tableName){
        return new Response().success();
    }

    @PostMapping("/generate")
    @ApiOperation(value = "生成代码",response = Response.class)
    public Response generate(@ApiParam(name ="generatorEntity", value = "配置对象")
                             @RequestBody GeneratorConfig generatorConfig){
    return new Response().success();
    }
}
