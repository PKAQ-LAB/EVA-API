package org.pkaq.sys.tenant.pkg.ctrl;

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
import org.pkaq.sys.tenant.pkg.bo.TenantPackageAoeBo;
import org.pkaq.sys.tenant.pkg.bo.TenantPackageQueryBo;
import org.pkaq.sys.tenant.pkg.service.TenantPackageService;
import org.springframework.web.bind.annotation.*;

/**
 * 租户套餐管理控制器。
 *
 * @author PKAQ
 */
@Tag(name = "租户套餐管理")
@RestController
@RequestMapping("/sys/tenant/package")
@RequiredArgsConstructor
public class TenantPackageCtrl extends Ctrl {
    private final TenantPackageService tenantPackageService;

    @PostMapping("/checkUnique")
    @Operation(summary = "校验套餐编码/名称唯一性")
    public Response<Object> checkUnique(@RequestBody TenantPackageAoeBo bo) {
        boolean exists = this.tenantPackageService.checkUnique(bo);
        return exists ? failure(CommonCodes.DUPLICATE_CODE_ERROR) : success();
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询租户套餐")
    @BizLog(operateType = BizLogCodes.QUERY, description = "查询了租户套餐列表[{0}]", args = {"param:0"})
    public Response<Object> list(TenantPackageQueryBo queryBo) {
        return success(this.tenantPackageService.list(queryBo));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID查询租户套餐")
    @BizLog(operateType = BizLogCodes.QUERY, description = "查询了租户套餐详情[{0}]", args = {"param:0"})
    public Response<Object> get(@Parameter(name = "id", description = "套餐ID")
                                @PathVariable("id") Long id) {
        return success(this.tenantPackageService.get(id));
    }

    @PostMapping("/edit")
    @Operation(summary = "新增/编辑租户套餐")
    @BizLog(operateType = BizLogCodes.EDIT, description = "编辑了租户套餐[{0}]", args = {"param:0"})
    public Response<Object> edit(@RequestBody @Valid TenantPackageAoeBo bo) {
        this.tenantPackageService.edit(bo);
        return success();
    }

    @PostMapping("/del")
    @Operation(summary = "批量删除租户套餐")
    @BizLog(operateType = BizLogCodes.DELETE, description = "删除了租户套餐[{0}]", args = {"param:0"})
    public Response<Object> del(@RequestBody SingleArray<Long> ids) {
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());
        this.tenantPackageService.delete(ids.getParam());
        return success();
    }

    @PostMapping("/switch")
    @Operation(summary = "切换租户套餐冻结状态")
    @BizLog(operateType = BizLogCodes.UPDATE, description = "切换了租户套餐冻结状态[{0}]", args = {"param:0"})
    public Response<Object> switchFrozen(@RequestBody SingleArray<Long> ids) {
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());
        this.tenantPackageService.switchFrozen(ids);
        return success();
    }
}
