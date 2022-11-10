package io.nerv.web.sys.module.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.ctrl.Ctrl;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.mvc.vo.SingleArray;
import io.nerv.web.sys.module.entity.ModuleEntityStd;
import io.nerv.web.sys.module.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 模块管理controller
 * @author: S.PKAQ
 */
@Tag(name = "模块管理")
@RestController
@RequestMapping("/sys/module")
@RequiredArgsConstructor
public class ModuleCtrl extends Ctrl {
    private final ModuleService service;

    @PostMapping("/checkUnique")
    @Operation(summary = "校验path唯一性")
    public Response checkUnique(@Parameter(name ="moduleEntity", description = "要进行校验的参数")
                                @RequestBody ModuleEntityStd module){

        boolean exist = (null != module && StrUtil.isNotBlank(module.getPath()))? this.service.checkUnique(module) : false;

        return exist? failure(BizCodeEnum.PATH_ALREADY_EXIST): success();
    }

    @PostMapping("/del")
    @Operation(summary = "根据ID删除/批量删除记录")
    public Response del(@Parameter(name = "ids", description = "[记录ID]")
                        @RequestBody SingleArray<String> ids){

        // 参数非空校验
        BizCodeEnum.NULL_ID.assertNotNull(ids);
        BizCodeEnum.NULL_ID.assertNotNull(ids.getParam());

        //如果Response不为空，则表示该节点下有子节点，返回错误给前台
        this.service.deleteModule(ids.getParam());

        return this.success();
    }

    @PostMapping("/edit")
    @Operation(summary = "新增/编辑记录")
    public Response save(@Parameter(name ="formdata", description = "模块对象")
                         @RequestBody ModuleEntityStd entity){
        this.service.editModule(entity);
        return this.success();
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID获得记录信息")
    public Response getRole(@Parameter(name = "id", description = "记录ID")
                            @PathVariable("id") String id){
        return this.success(this.service.getModule(id));
    }

    @GetMapping({"/listModuleByAttr","/listNoPage"})
    @Operation(summary = "根据实体类属性获取相应的模块树 ")
    public Response listModuleByAttr(@Parameter(name = "module", description= "{key: value}") ModuleEntityStd module){
        return success(this.service.listModuleByAttr(module));
    }

    @PostMapping("/sort")
    @Operation(summary = "排序模块信息")
    public Response sortModule(@Parameter(name = "module", description = "{id,orders}")
                               @RequestBody ModuleEntityStd[] switchObj){
        this.service.sortModule(switchObj);
        return success();
    }

    @PostMapping("/switchStatus")
    @Operation(summary = "切换模块可用状态")
    public Response switchStatus(@Parameter(name = "id", description = "模块Id")
                                 @RequestBody ModuleEntityStd module){
        this.service.disableChild(module);
        return success();
    }
}
