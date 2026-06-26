package org.pkaq.sys.organization.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.util.StrUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.organization.bo.OrganizationAoeBo;
import org.pkaq.sys.organization.bo.OrganizationQueryBo;
import org.pkaq.sys.organization.bo.OrganizationSortBo;
import org.pkaq.sys.organization.service.OrganizationService;
import org.springframework.web.bind.annotation.*;

/**
 * 组织管理 Controller（树形）
 *
 * @author PKAQ
 */
@Tag(name = "组织管理")
@RestController
@RequestMapping("/sys/organization")
@RequiredArgsConstructor
public class OrganizationCtrl extends Ctrl {
    private final OrganizationService service;

    @PostMapping("/checkUnique")
    @Operation(summary = "校验 code / name 在同 pid 下是否唯一")
    public Response<Object> checkUnique(@Parameter(name = "bo", description = "组织唯一性校验参数")
                                        @RequestBody OrganizationAoeBo bo) {
        if (StrUtils.isAllBlank(bo.getCode(), bo.getName())) {
            return failure(SysCodes.MISS_CODE_OR_NAME);
        }
        boolean exists = this.service.checkUnique(bo);
        return exists ? failure(SysCodes.ORG_CODE_EXIST) : success();
    }

    @GetMapping("/list")
    @Operation(summary = "获取组织树")
    @BizLog(operateType = BizLogCodes.QUERY, description = "查询了组织树[{0}]", args = {"param:0"})
    public Response<Object> list(@Parameter(name = "queryBo", description = "请求参数") OrganizationQueryBo queryBo) {
        return success(this.service.list(queryBo));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据 ID 获取组织详情")
    @BizLog(operateType = BizLogCodes.QUERY, description = "查询了组织信息[{0}]", args = {"param:0"})
    public Response<Object> get(@Parameter(name = "id", description = "组织ID")
                                @PathVariable("id") Long id) {
        return success(this.service.get(id));
    }

    @PostMapping("/edit")
    @Operation(summary = "新增 / 编辑组织")
    @BizLog(operateType = BizLogCodes.EDIT, description = "编辑了组织信息[{0}]", args = {"param:0"})
    public Response<Object> edit(@Parameter(name = "bo", description = "组织对象")
                                 @RequestBody @Valid OrganizationAoeBo bo) {
        this.service.edit(bo);
        return success();
    }

    @PostMapping("/del")
    @Operation(summary = "根据 ID 批量删除")
    @BizLog(operateType = BizLogCodes.DELETE, description = "删除了组织[{0}]", args = {"param:0"})
    public Response<Object> del(@Parameter(name = "ids", description = "[组织ID]")
                                @RequestBody SingleArray<Long> ids) {
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());
        this.service.delete(ids.getParam());
        return success();
    }

    @PostMapping("/sort")
    @Operation(summary = "同级拖拽排序")
    @BizLog(operateType = BizLogCodes.UPDATE, description = "调整了组织顺序[{0}]", args = {"param:0"})
    public Response<Object> sort(@Parameter(name = "bo", description = "{id, oldSort, newSort}")
                                 @RequestBody OrganizationSortBo bo) {
        this.service.sort(bo);
        return success();
    }

    @PostMapping("/switch")
    @Operation(summary = "切换冻结状态（级联子节点）")
    @BizLog(operateType = BizLogCodes.UPDATE, description = "切换了组织冻结状态[{0}]", args = {"param:0"})
    public Response<Object> switchFrozen(@Parameter(name = "ids", description = "[组织Id]")
                                         @RequestBody SingleArray<Long> ids) {
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());
        this.service.switchFrozen(ids);
        return success();
    }
}
