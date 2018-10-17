package org.pkaq.web.sys.module.ctrl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pkaq.core.mvc.ctrl.BaseCtrl;
import org.pkaq.core.mvc.util.Response;
import org.pkaq.web.sys.module.entity.ModuleEntity;
import org.pkaq.web.sys.module.service.ModuleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模块管理controller
 * @author: S.PKAQ
 * @Datetime: 2018/3/28 19:20
 */
@Api( description = "模块管理")
@RestController
@RequestMapping("/module")
public class ModuleCtrl extends BaseCtrl<ModuleService, ModuleEntity>{

    @PostMapping("/checkUnique")
    @ApiOperation(value = "校验path唯一性",response = Response.class)
    public Response checkUnique(@ApiParam(name ="moduleEntity", value = "要进行校验的参数")
                                @RequestBody ModuleEntity module){
        boolean exist = this.service.checkUnique(module);
        return exist? failure(): success();
    }

    @GetMapping("/listModuleByAttr")
    @ApiOperation(value = "根据实体类属性获取相应的模块树 ", response = Response.class)
    public Response listModuleByAttr(@ApiParam(name = "module", value= "{key: value}") ModuleEntity module){
        return success(this.service.listModuleByAttr(module));
    }

    @PostMapping("/sort")
    @ApiOperation(value = "排序模块信息", response = Response.class)
    public Response sortModule(@ApiParam(name = "module", value = "{id,orders}")
                               @RequestBody ModuleEntity[] switchObj){
        this.service.sortModule(switchObj);
        return success(this.service.listModule(null));
    }

    @PostMapping("/switchStatus")
    @ApiOperation(value = "切换模块可用状态", response = Response.class)
    public Response switchStatus(@ApiParam(name = "id", value = "模块Id")
                                 @RequestBody ModuleEntity module){
        this.service.updateModule(module);
        return success();
    }
}
