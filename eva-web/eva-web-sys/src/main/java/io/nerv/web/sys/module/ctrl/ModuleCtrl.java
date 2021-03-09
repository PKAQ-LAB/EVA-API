package io.nerv.web.sys.module.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.ParamException;
import io.nerv.core.mvc.ctrl.Ctrl;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.mvc.vo.SingleArray;
import io.nerv.web.sys.module.entity.ModuleEntity;
import io.nerv.web.sys.module.service.ModuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 模块管理controller
 * @author: S.PKAQ
 * @Datetime: 2018/3/28 19:20
 */
@Api("模块管理")
@RestController
@RequestMapping("/sys/module")
@RequiredArgsConstructor
public class ModuleCtrl extends Ctrl {
    private final ModuleService service;

    @PostMapping("/checkUnique")
    @ApiOperation(value = "校验path唯一性",response = Response.class)
    public Response checkUnique(@ApiParam(name ="moduleEntity", value = "要进行校验的参数")
                                @RequestBody ModuleEntity module){
        boolean exist = this.service.checkUnique(module);
        return exist? failure(BizCodeEnum.PATH_ALREADY_EXIST): success();
    }

    @PostMapping("/del")
    @ApiOperation(value = "根据ID删除/批量删除记录",response = Response.class)
    public Response del(@ApiParam(name = "ids", value = "[记录ID]")
                        @RequestBody SingleArray<String> ids){

        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }

        //如果Response不为空，则表示该节点下有子节点，返回错误给前台
        this.service.deleteModule(ids.getParam());

        return this.success();
    }

    @PostMapping("/edit")
    @ApiOperation(value = "新增/编辑记录",response = Response.class)
    public Response save(@ApiParam(name ="formdata", value = "模块对象")
                         @RequestBody ModuleEntity entity){
        this.service.editModule(entity);
        return this.success();
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "根据ID获得记录信息", response = Response.class)
    public Response getRole(@ApiParam(name = "id", value = "记录ID")
                            @PathVariable("id") String id){
        return this.success(this.service.getModule(id));
    }

    @GetMapping({"/listModuleByAttr","/listNoPage"})
    @ApiOperation(value = "根据实体类属性获取相应的模块树 ", response = Response.class)
    public Response listModuleByAttr(@ApiParam(name = "module", value= "{key: value}") ModuleEntity module){
        return success(this.service.listModuleByAttr(module));
    }

    @PostMapping("/sort")
    @ApiOperation(value = "排序模块信息", response = Response.class)
    public Response sortModule(@ApiParam(name = "module", value = "{id,orders}")
                               @RequestBody ModuleEntity[] switchObj){
        this.service.sortModule(switchObj);
        return success();
    }

    @PostMapping("/switchStatus")
    @ApiOperation(value = "切换模块可用状态", response = Response.class)
    public Response switchStatus(@ApiParam(name = "id", value = "模块Id")
                                 @RequestBody ModuleEntity module){
        this.service.updateModule(module);
        return success();
    }
}
