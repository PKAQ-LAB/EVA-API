package org.pkaq.web.organization.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pkaq.core.mvc.BaseCtrl;
import org.pkaq.core.util.Response;
import org.pkaq.core.util.SingleArray;
import org.pkaq.web.organization.entity.OrganizationEntity;
import org.pkaq.web.organization.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

/**
 * 组织管理
 * Datetime: 2018/2/9 10:55
 * @author PKAQ
 */
@Api( description = "组织管理")
@RestController
@RequestMapping("/organization")
public class OrgCtrl extends BaseCtrl {
    @Autowired
    private OrganizationService organizationService;
    /**
     * 查询组织列表树
     * @param organization
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取组织列表",response = Response.class)
    public Response listOrg(@ApiParam(name = "name", value = "组织名称或编码") OrganizationEntity organization){
        return new Response().success(this.organizationService.listOrg(organization));
    }

    /**
     * 根据ID获取一条组织信息
     * @param id
     * @return
     */
    @GetMapping("/get")
    @ApiOperation(value = "根据ID获取组织信息", response = Response.class)
    public Response getOrg(@ApiParam(name = "id", value = "组织ID") String id){
        return new Response().success();
    }

    /**
     * 根据ID删除组织(必需)
     * @param ids
     * @return
     */
    @PostMapping("/del")
    @ApiOperation(value = "根据ID删除/批量删除组织", response = Response.class)
    public Response delOrg(@ApiParam(name = "ids", value = "[组织ID]") @RequestBody SingleArray ids){
        // 参数非空校验
        if (null == ids || CollectionUtil.isEmpty(ids.getIds())){
            return new Response().failure(400, this.getI18NHelper().getMessage("param_id_notnull"));
        }

        return this.organizationService.deleteOrg(ids.getIds());
    }

    /**
     * 编辑组织信息
     * @param organization
     * @return
     */
    @PostMapping("/edit")
    @ApiOperation(value = "编辑组织信息", response = Response.class)
    public Response editOrg(@ApiParam(name ="organization", value = "组织信息") @RequestBody OrganizationEntity organization){
        String orgId = organization.getId();

        Integer operate;

        Console.error(organization.getName());
        // 有ID更新，无ID新增
        if(StrUtil.isNotBlank(orgId)){
            operate = this.organizationService.updateOrg(organization);
        }else{
            operate = this.organizationService.insertOrg(organization);
        }

        return new Response().success(operate);
    }

    /**
     * 排序
     * @param organization
     * @return
     */
    @PostMapping("/sort")
    @ApiOperation(value = "排序组织信息", response = Response.class)
    public Response sortOrg(@ApiParam(name = "organization", value = "{id,orders}")  OrganizationEntity organization){
        this.organizationService.updateOrg(organization);
        return new Response().success();
    }

    /**
     * 切换可用状态
     * @param organization
     * @return
     */
    @PostMapping("/switchStatus")
    @ApiOperation(value = "切换组织可用状态", response = Response.class)
    public Response switchStatus(@ApiParam(name = "id", value = "组织Id") OrganizationEntity organization){
        this.organizationService.updateOrg(organization);
        return new Response().success();
    }
}
