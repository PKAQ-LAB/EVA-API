package org.pkaq.web.organization.ctrl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pkaq.core.util.Response;
import org.pkaq.web.organization.entity.OrganizationEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
public class OrgCtrl {
    /**
     * 查询组织列表树
     * @param name
     * @return
     */
    @GetMapping("/listOrg")
    @ApiOperation(value = "获取组织列表",response = Response.class)
    public Response listOrg(@ApiParam(name = "name", value = "组织名称或编码") String name){
        return new Response().success();
    }

    /**
     * 根据ID获取一条组织信息
     * @param id
     * @return
     */
    @GetMapping("/getOrg")
    @ApiOperation(value = "根据ID获取组织信息", response = Response.class)
    public Response getOrg(@ApiParam(name = "id", value = "组织ID") String id){
        return new Response().success();
    }

    /**
     * 根据ID删除组织
     * @param ids
     * @return
     */
    @PostMapping("/delOrg")
    @ApiOperation(value = "根据ID删除/批量删除组织", response = Response.class)
    public Response delOrg(@ApiParam(name = "ids", value = "[组织ID]") String ids){
        return new Response().success();
    }

    /**
     * 编辑组织信息
     * @param organization
     * @return
     */
    @PostMapping("/editOrg")
    @ApiOperation(value = "编辑组织信息", response = Response.class)
    public Response editOrg(@ApiParam(name ="organization", value = "组织信息") OrganizationEntity organization){
        return new Response().success();
    }

    /**
     * 排序
     * @param organization
     * @return
     */
    @PostMapping("/sortOrg")
    @ApiOperation(value = "排序组织信息", response = Response.class)
    public Response sortOrg(@ApiParam(name = "organization", value = "{id,orders}")  OrganizationEntity organization){
        return new Response().success();
    }

    /**
     * 切换可用状态
     * @param id
     * @return
     */
    @PostMapping("/switchStatus")
    @ApiOperation(value = "切换组织可用状态", response = Response.class)
    public Response switchStatus(@ApiParam(name = "id", value = "组织Id") String id){
        return new Response().success();
    }
}
