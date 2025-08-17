package org.pkaq.sys.tenant.ctrl;

import cn.hutool.core.text.CharSequenceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.tenant.bo.TenantAoeBo;
import org.pkaq.sys.tenant.bo.TenantCheckBo;
import org.pkaq.sys.tenant.bo.TenantQueryBo;
import org.pkaq.sys.tenant.service.TenantService;
import org.pkaq.sys.tenant.vo.TenantDetailVo;
import org.pkaq.sys.tenant.vo.TenantListVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 租户管理控制器
 *
 * @author PKAQ
 */
@Tag(name = "租户管理")
@RestController
@RequestMapping("/sys/tenant")
@RequiredArgsConstructor
public class TenantCtrl extends Ctrl {
    private final TenantService service;

    @PostMapping("/checkUnique")
    @Operation(summary = "校验租户code/name唯一性")
    public Response<Object> checkUnique(@Parameter(name = "checkBo", description = "要进行校验的参数")
                                        @RequestBody TenantCheckBo checkBo) {
        if (null == checkBo.getCode() && null == checkBo.getName()) {
            CommonCodes.PARAM_LOST.newException();
        }
        var exist = this.service.checkUnique(checkBo);

        if (exist) {
            if (CharSequenceUtil.isBlank(checkBo.getName())) {
                return failure(SysCodes.TENANT_CODE_ALREADY_EXIST);
            } else if (CharSequenceUtil.isBlank(checkBo.getCode())) {
                return failure(SysCodes.TENANT_NAME_ALREADY_EXIST);
            } else {
                return failure(SysCodes.TENANT_CODE_OR_NAME_ALREADY_EXIST);
            }
        }
        return success();
    }

    @PostMapping("/edit")
    @Operation(summary = "新增/编辑记录")
    @BizLog(operateType = BizLogCodes.EDIT, description = "编辑了租户信息[{0}]", args = {"param:0"})
    public Response<Object> edit(@Parameter(name = "formdata", description = "租户对象")
                                 @RequestBody @Validated TenantAoeBo bo) {
        this.service.edit(bo);
        return this.success();
    }

    @GetMapping("/list")
    @Operation(summary = "根据条件查询列表数据 ")
    @BizLog(operateType = BizLogCodes.QUERY, description = "查询了租户列表[{0}]", args = {"param:0"})
    public Response<PageVo<TenantListVo>> list(@Parameter(name = "queryBo", description = "请求参数")
                                               TenantQueryBo queryBo) {
        return success(this.service.listPage(queryBo));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID获得租户信息")
    @BizLog(operateType = BizLogCodes.QUERY, description = "查询了租户信息[{0}]", args = {"param:0"})
    public Response<TenantDetailVo> getRole(@Parameter(name = "id", description = "记录ID")
                                            @PathVariable("id") Long id) {
        return this.success(this.service.get(id));
    }

    @PostMapping("/switch")
    @BizLog(operateType = BizLogCodes.EDIT, description = "调整了租户状态[{0}]", args = {"param:0"})
    @Operation(summary = "切换租户可用状态")
    public Response<Object> change(@RequestBody SingleArray<Long> ids) {
        this.service.switchFrozen(ids);
        return success();
    }

    @PostMapping("/del")
    @Operation(summary = "根据ID删除/批量删除记录")
    @BizLog(operateType = BizLogCodes.DELETE, description = "删除了租户信息[{0}]", args = {"param:0"})
    public Response<Object> del(@Parameter(name = "ids", description = "[记录ID]")
                                @RequestBody SingleArray<Long> ids) {
        // 参数非空校验
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());
        this.service.delete(ids.getParam());
        return this.success();
    }
}
