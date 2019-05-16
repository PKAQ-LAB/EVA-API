package io.nerv.web.sys.module.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import io.nerv.core.mvc.ctrl.PureBaseCtrl;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.mvc.util.SingleArray;
import io.nerv.web.sys.module.entity.ModuleEntity;
import io.nerv.web.sys.module.service.ModuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.nerv.core.exception.ParamException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模块管理controller
 * @author: S.PKAQ
 * @Datetime: 2018/3/28 19:20
 */
@Api("模块管理")
@RestController
@RequestMapping("/module")
public class ModuleCtrl extends PureBaseCtrl<ModuleService> {

    @PostMapping("/checkUnique")
    @ApiOperation(value = "校验path唯一性",response = Response.class)
    public Response checkUnique(@ApiParam(name ="moduleEntity", value = "要进行校验的参数")
                                @RequestBody ModuleEntity module){
        boolean exist = this.service.checkUnique(module);
        return exist? failure(): success();
    }

    @PostMapping("/del")
    @ApiOperation(value = "根据ID删除/批量删除记录",response = Response.class)
    public Response del(@ApiParam(name = "ids", value = "[记录ID]")
                        @RequestBody SingleArray<String> ids){

        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }

        //如果Response不为空，则表示该节点下有子节点，返回错误给前台
        Response response=this.service.deleteModule(ids.getParam());
        if(response != null){
            return response;
        }

        return this.success(this.service.listModule(null));
    }

    @PostMapping("edit")
    @ApiOperation(value = "新增/编辑记录",response = Response.class)
    public Response save(@ApiParam(name ="formdata", value = "模块对象")
                         @RequestBody ModuleEntity entity){
       Response response = this.service.editModule(entity);

        //如果Response不为空，则表示有返回错误给前台
        if(response != null){
            return response;
        }
        return success(   this.service.listModule(null));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "根据ID获得记录信息", response = Response.class)
    public Response getRole(@ApiParam(name = "id", value = "记录ID")
                            @PathVariable("id") String id){
        return this.success(this.service.getById(id));
    }

    @GetMapping({"listModuleByAttr","listNoPage"})
    @ApiOperation(value = "根据实体类属性获取相应的模块树 ", response = Response.class)
    public Response listModuleByAttr(@ApiParam(name = "module", value= "{key: value}") ModuleEntity module){
        return success(this.service.listModuleByAttr(module));
    }

    @PostMapping("/sort")
    @ApiOperation(value = "排序模块信息", response = Response.class)
    public Response sortModule(@ApiParam(name = "module", value = "{id,orders}")
                               @RequestBody ModuleEntity[] switchObj){
        this.service.sortModule(switchObj);
        return success(this.service.listModuleByAttr(null));
    }

    @PostMapping("/switchStatus")
    @ApiOperation(value = "切换模块可用状态", response = Response.class)
    public Response switchStatus(@ApiParam(name = "id", value = "模块Id")
                                 @RequestBody ModuleEntity module){
        this.service.updateModule(module);
        return success(this.service.listModule(null));
    }
}
