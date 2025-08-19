package org.pkaq.sys.module.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.mybatis.mvc.ctrl.StdCtrl;
import org.pkaq.sys.module.bo.ModuleAoeBo;
import org.pkaq.sys.module.bo.ModuleQueryBo;
import org.pkaq.sys.module.bo.ModuleSortBo;
import org.pkaq.sys.module.service.ModuleService;
import org.springframework.web.bind.annotation.*;

/**
 * 模块管理controller
 * @author: S.PKAQ
 */
@Tag(name = "模块管理")
@RestController
@RequestMapping("/sys/module")
@RequiredArgsConstructor
public class ModuleCtrl extends StdCtrl<ModuleService> {

    @Override
    @PostMapping("/del")
    @Operation(summary = "根据ID删除/批量删除记录")
    @BizLog(operateType = BizLogCodes.DELETE, description = "删除了模块[{0}]", args = {"param:0"})
    public Response<Object> del(@Parameter(name = "ids", description = "[记录ID]")
                                @RequestBody SingleArray<Long> ids) {
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());
        this.service.deleteModule(ids.getParam());

        return this.success();
    }

    @PostMapping("/edit")
    @Operation(summary = "新增/编辑记录")
    @BizLog(operateType = BizLogCodes.EDIT, description = "编辑了模块信息[{0}]", args = {"param:0"})
    public Response<Object> edit(@Parameter(name = "bo", description = "模块对象")
                                 @RequestBody ModuleAoeBo bo) {
        this.service.editModule(bo);
        return this.success();
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID获得记录信息")
    @BizLog(operateType = BizLogCodes.EDIT, description = "插叙了模块信息[{0}]", args = {"param:0"})
    public Response<Object> getRole(@Parameter(name = "id", description = "记录ID")
                                    @PathVariable("id") Long id) {
        return this.success(this.service.getModule(id));
    }

    @GetMapping({"/list"})
    @Operation(summary = "根据实体类属性获取相应的模块树 ")
    @BizLog(operateType = BizLogCodes.EDIT, description = "查询了模块树[{0}]", args = {"param:0"})
    public Response<Object> list(@Parameter(name = "module", description = "{key: value}") ModuleQueryBo queryBo) {
        return success(this.service.list(queryBo, false));
    }

    @PostMapping("/sort")
    @Operation(summary = "排序模块信息")
    @BizLog(operateType = BizLogCodes.EDIT, description = "调整了模块顺序[{0}]", args = {"param:0"})
    public Response<Object> sortModule(@Parameter(name = "module", description = "{id,orders}")
                                       @RequestBody ModuleSortBo switchObj) {
        this.service.sortModule(switchObj);
        return success();
    }

    @PostMapping("/switchStatus")
    @Operation(summary = "切换模块可用状态")
    public Response<Object> switchStatus(@Parameter(name = "id", description = "模块Id")
                                         @RequestBody SingleArray<String> ids) {
        return success();
    }
}
