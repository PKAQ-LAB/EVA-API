package org.pkaq.sys.module.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mvc.bo.IdCodeBo;
import org.pkaq.core.mvc.bo.SingleArrayBo;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.module.bo.ModuleAoeBo;
import org.pkaq.sys.module.bo.ModuleQueryBo;
import org.pkaq.sys.module.bo.ModuleSortBo;
import org.pkaq.sys.module.service.ModuleService;
import org.springframework.web.bind.annotation.*;

/**
 * 模块管理controller
 *
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
    public Response<Object> checkUnique(@Parameter(name = "idCodeBo", description = "要进行校验的参数")
                                @RequestBody IdCodeBo idCodeBo) {

        return this.service.isUnique(idCodeBo) ? failure(SysCodes.CODE_EXIST) : success();
    }

    @PostMapping("/del")
    @Operation(summary = "根据ID删除/批量删除记录")
    public Response<Object> del(@Parameter(name = "ids", description = "[记录ID]")
                                @RequestBody SingleArrayBo<String> ids) {

        // 参数非空校验
        CommonCodes.NULL_ID.assertNotNull(ids);
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());

        //如果Response不为空，则表示该节点下有子节点，返回错误给前台
        this.service.deleteModule(ids.getParam());

        return this.success();
    }

    @PostMapping("/edit")
    @Operation(summary = "新增/编辑记录")
    public Response<Object> edit(@Parameter(name = "bo", description = "模块对象")
                                @RequestBody ModuleAoeBo bo) {
        this.service.editModule(bo);
        return this.success();
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID获得记录信息")
    public Response<Object> getRole(@Parameter(name = "id", description = "记录ID")
                                    @PathVariable("id") String id) {
        return this.success(this.service.getModule(id));
    }

    @GetMapping({"/listModuleByAttr", "/listNoPage"})
    @Operation(summary = "根据实体类属性获取相应的模块树 ")
    public Response<Object> listModuleByAttr(@Parameter(name = "module", description = "{key: value}") ModuleQueryBo queryBo) {
        return success(this.service.listModuleByAttr(queryBo));
    }

    @PostMapping("/sort")
    @Operation(summary = "排序模块信息")
    public Response<Object> sortModule(@Parameter(name = "module", description = "{id,orders}")
                                       @RequestBody ModuleSortBo[] switchObj) {
        this.service.sortModule(switchObj);
        return success();
    }

    @PostMapping("/switchStatus")
    @Operation(summary = "切换模块可用状态")
    public Response<Object> switchStatus(@Parameter(name = "id", description = "模块Id")
                                         @RequestBody SingleArrayBo<String> ids) {
        this.service.disableChild(ids);
        return success();
    }
}
