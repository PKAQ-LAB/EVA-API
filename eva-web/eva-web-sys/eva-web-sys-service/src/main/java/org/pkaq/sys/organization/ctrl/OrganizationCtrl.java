package org.pkaq.sys.organization.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mvc.bo.IdCodeBo;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.organization.bo.OrganizationAoeBo;
import org.pkaq.sys.organization.bo.OrganizationQueryBo;
import org.pkaq.sys.organization.bo.OrganizationSortBo;
import org.pkaq.sys.organization.service.OrganizationService;
import org.springframework.web.bind.annotation.*;

/**
 * 组织管理
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
    @Operation(summary = "校验code唯一性")
    public Response<Object> checkUnique(@Parameter(name = "idCodeBo", description = "要进行校验的参数")
                                        @RequestBody @Valid IdCodeBo idCodeBo) {
        return this.service.isUnique(idCodeBo) ? failure(SysCodes.ORG_CODE_EXIST) : success();
    }

    @GetMapping("/list")
    @Operation(summary = "根据实体类属性获取相应的组织树 ")
    public Response<Object> listOrgByAttr(@Parameter(name = "queryBo", description = "{key: value}") OrganizationQueryBo queryBo) {
        return success(this.service.list(queryBo));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID获取详情")
    public Response<Object> getOrg(@Parameter(name = "id", description = "组织ID")
                                   @PathVariable("id") Long id) {
        return success(this.service.getOrg(id));
    }

    @PostMapping("/del")
    @Operation(summary = "根据ID删除/批量删除组织")
    //@PreAuthorize("hasRole('ADMIN')")
    public Response<Object> delOrg(@Parameter(name = "ids", description = "[组织ID]")
                                   @RequestBody SingleArray<Long> ids) {
        // 参数非空校验
        CommonCodes.NULL_ID.assertNotNull(ids);
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());

        // 判断上级节点是否还有其它叶子 如果没有把 isleaf属性改为false
        this.service.deleteOrg(ids.getParam());

        return success();
    }

    @PostMapping("/edit")
    @Operation(summary = "编辑组织信息")
    public Response<Object> editOrg(@Parameter(name = "organization", description = "组织信息")
                                    @RequestBody OrganizationAoeBo bo) {
        this.service.editOrg(bo);
        return success();
    }

    @PostMapping("/sort")
    @Operation(summary = "排序组织信息")
    public Response<Object> sortOrg(@Parameter(name = "organization", description = "{id,orders}")
                                    @RequestBody OrganizationSortBo[] switchObj) {
        this.service.sortOrg(switchObj);
        return success();
    }

    @PostMapping("/switchStatus")
    @Operation(summary = "切换组织可用状态")
    public Response<Object> switchStatus(@Parameter(name = "id", description = "组织Id")
                                         @RequestBody SingleArray<String> ids) {
        this.service.switchStatus(ids);
        return success();
    }
}
