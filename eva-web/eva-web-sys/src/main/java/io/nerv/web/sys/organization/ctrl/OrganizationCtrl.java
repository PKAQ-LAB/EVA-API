package io.nerv.web.sys.organization.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.ParamException;
import io.nerv.core.mvc.ctrl.Ctrl;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.mvc.vo.SingleArray;
import io.nerv.web.sys.organization.entity.OrganizationEntity;
import io.nerv.web.sys.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 组织管理
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
    public Response checkUnique(@Parameter(name ="organization", description = "要进行校验的参数")
                                @RequestBody OrganizationEntity organization){
        boolean exist = (null != organization && StrUtil.isNotBlank(organization.getCode()))? this.service.checkUnique(organization) : false;
        return exist? failure(BizCodeEnum.ORG_CODE_EXIST): success();
    }

    @GetMapping("/list")
    @Operation(summary = "根据实体类属性获取相应的组织树 ")
    public Response listOrgByAttr(@Parameter(name = "organization", description= "{key: value}") OrganizationEntity organization){
       return success(this.service.list(organization));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID获取组织信息")
    public Response getOrg(@Parameter(name = "id", description = "组织ID")
                           @PathVariable("id") String id){
        OrganizationEntity entity = this.service.getOrg(id);
        return success(entity);
    }

    @PostMapping("/del")
    @Operation(summary = "根据ID删除/批量删除组织")
    //@PreAuthorize("hasRole('ADMIN')")
    public Response delOrg(@Parameter(name = "ids", description = "[组织ID]")
                           @RequestBody SingleArray<String> ids){
        // 参数非空校验
        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
           throw new ParamException(locale("param_id_notnull"));
        }
        // 判断上级节点是否还有其它叶子 如果没有把 isleaf属性改为false
        return this.service.deleteOrg(ids.getParam());
    }

    @PostMapping("/edit")
    @Operation(summary = "编辑组织信息")
    public Response editOrg(@Parameter(name ="organization", description = "组织信息")
                            @RequestBody OrganizationEntity organization){
        this.service.editOrg(organization);
        return success();
    }

    @PostMapping("/sort")
    @Operation(summary = "排序组织信息")
    public Response sortOrg(@Parameter(name = "organization", description = "{id,orders}")
                            @RequestBody OrganizationEntity[] switchObj){
        this.service.sortOrg(switchObj);
        return success();
    }

    @PostMapping("/switchStatus")
    @Operation(summary = "切换组织可用状态")
    public Response switchStatus(@Parameter(name = "id", description = "组织Id")
                                 @RequestBody OrganizationEntity organization){
        this.service.switchStatus(organization);
        return success();
    }
}
