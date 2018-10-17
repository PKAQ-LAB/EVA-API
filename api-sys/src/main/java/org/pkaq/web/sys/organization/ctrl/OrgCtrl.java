package org.pkaq.web.sys.organization.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pkaq.core.exception.ParamException;
import org.pkaq.core.mvc.ctrl.BaseCtrl;
import org.pkaq.core.mvc.ctrl.PureBaseCtrl;
import org.pkaq.core.mvc.util.Response;
import org.pkaq.core.mvc.util.SingleArray;
import org.pkaq.web.sys.organization.entity.OrganizationEntity;
import org.pkaq.web.sys.organization.service.OrganizationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 组织管理
 * Datetime: 2018/2/9 10:55
 * @author PKAQ
 */
@Api( description = "组织管理")
@RestController
@RequestMapping("/organization")
public class OrgCtrl extends PureBaseCtrl<OrganizationService> {

    @PostMapping("/checkUnique")
    @ApiOperation(value = "校验code唯一性",response = Response.class)
    public Response checkUnique(@ApiParam(name ="organization", value = "要进行校验的参数")
                                @RequestBody OrganizationEntity organization){
        boolean exist = this.service.checkUnique(organization);
        return exist? failure(): success();
    }
    @GetMapping({"/list","/list/{condition}"})
    @ApiOperation(value = "获取组织列表",response = Response.class)
    public Response listOrg(@ApiParam(name = "condition", value = "组织名称或编码")
                            @PathVariable(value = "condition", required = false) String condition){
        return success(this.service.listOrg(condition));
    }

    @GetMapping("/listOrgByAttr")
    @ApiOperation(value = "根据实体类属性获取相应的组织树 ", response = Response.class)
    public Response listOrgByAttr(@ApiParam(name = "organization", value= "{key: value}") OrganizationEntity organization){
       return success(this.service.listOrgByAttr(organization));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "根据ID获取组织信息", response = Response.class)
    public Response getOrg(@ApiParam(name = "id", value = "组织ID")
                           @PathVariable("id") String id){
        OrganizationEntity entity = this.service.getOrg(id);
        return success(entity);
    }

    @PostMapping("/del")
    @ApiOperation(value = "根据ID删除/批量删除组织", response = Response.class)
    @PreAuthorize("hasRole('ADMIN')")
    public Response delOrg(@ApiParam(name = "ids", value = "[组织ID]")
                           @RequestBody SingleArray<String> ids){
        // 参数非空校验
        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
           throw new ParamException(locale("param_id_notnull"));
        }
        // 判断上级节点是否还有其它叶子 如果没有把 isleaf属性改为false
        return this.service.deleteOrg(ids.getParam());
    }

    @PostMapping("/edit")
    @ApiOperation(value = "编辑组织信息", response = Response.class)
    public Response editOrg(@ApiParam(name ="organization", value = "组织信息")
                            @RequestBody OrganizationEntity organization){
        return success(this.service.editOrg(organization));
    }

    @PostMapping("/sort")
    @ApiOperation(value = "排序组织信息", response = Response.class)
    public Response sortOrg(@ApiParam(name = "organization", value = "{id,orders}")
                            @RequestBody OrganizationEntity[] switchObj){
        this.service.sortOrg(switchObj);
        return success(this.service.listOrg(null));
    }

    @PostMapping("/switchStatus")
    @ApiOperation(value = "切换组织可用状态", response = Response.class)
    public Response switchStatus(@ApiParam(name = "id", value = "组织Id")
                                 @RequestBody OrganizationEntity organization){
        this.service.switchStatus(organization);
        return success(this.service.listOrg(null));
    }
}