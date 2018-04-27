package org.pkaq.sys.module.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pkaq.core.exception.ParamException;
import org.pkaq.core.mvc.ctrl.BaseCtrl;
import org.pkaq.core.mvc.util.Response;
import org.pkaq.core.mvc.util.SingleArray;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.module.service.ModuleService;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Api( description = "模块管理")
@RestController
@RequestMapping("/module")
public class ModuleCtrl extends BaseCtrl<ModuleService>{

    @GetMapping({"/list","/list/{condition}"})
    @ApiOperation(value = "获取模块列表",response = Response.class)
    public Response listModule(@ApiParam(name = "condition", value = "模块名称或编码")
                               @PathVariable(value = "condition", required = false) String condition){
        return success(this.service.listModule(condition));
    }

    @GetMapping("/listModuleByAttr")
    @ApiOperation(value = "根据实体类属性获取相应的模块树 ", response = Response.class)
    public Response listModuleByAttr(@ApiParam(name = "module", value= "{key: value}") ModuleEntity module){
        return success(this.service.listModuleByAttr(module));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "根据ID获取模块信息", response = Response.class)
    public Response getModule(@ApiParam(name = "id", value = "模块ID")
                              @PathVariable("id") String id){
        ModuleEntity entity = this.service.getModule(id);
        return success(entity);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/del")
    @ApiOperation(value = "根据ID删除/批量删除模块", response = Response.class)
    public Response delModule(@ApiParam(name = "ids", value = "[模块ID]")
                              @RequestBody SingleArray<String> ids){
        // 参数非空校验
        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }
        // 判断上级节点是否还有其它叶子 如果没有把 isleaf属性改为false
        return this.service.deleteModule(ids.getParam());
    }

    @PostMapping("/edit")
    @ApiOperation(value = "新增/编辑模块信息", response = Response.class)
    public Response editModule(@ApiParam(name ="module", value = "模块信息")
                               @RequestBody ModuleEntity module){
        return success(this.service.editModule(module));
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
