package org.pkaq.web.sys.role.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pkaq.core.exception.ParamException;
import org.pkaq.core.mvc.ctrl.BaseCtrl;
import org.pkaq.core.mvc.util.Response;
import org.pkaq.core.mvc.util.SingleArray;
import org.pkaq.web.sys.role.service.RoleService;
import org.pkaq.web.sys.role.entity.RoleEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色管理
 * @author: S.PKAQ
 * @Datetime: 2018/4/13 7:27
 */
@Api( description = "角色管理")
@RestController
@RequestMapping("/role")
public class RoleCtrl extends BaseCtrl<RoleService> {
    @PostMapping("/checkUnique")
    @ApiOperation(value = "校验账号唯一性",response = Response.class)
    public Response checkUnique(@ApiParam(name ="roleEntity", value = "要进行校验的参数")
                                @RequestBody RoleEntity role){
        boolean exist = this.service.checkUnique(role);
        return exist? failure(): success();
    }
    @GetMapping({"/list"})
    @ApiOperation(value = "获取角色列表", response = Response.class)
    public Response listRole(@ApiParam(name = "roleEntity", value = "包含角色对象属性的查询条件")
                                     RoleEntity roleEntity, Integer page) {
        return success(this.service.listRole(roleEntity, page));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "根据ID获取角色信息", response = Response.class)
    public Response getRole(@ApiParam(name = "id", value = "角色ID")
                            @PathVariable("id") String id){
        RoleEntity entity = this.service.getRole(id);
        return success(entity);
    }

    @PostMapping("/save")
    @ApiOperation(value = "新增/编辑角色信息", response = Response.class)
    public Response saveRole(@ApiParam(name ="role", value = "角色信息")
                             @RequestBody RoleEntity role){
        return success(this.service.saveRole(role));
    }

    @PostMapping("/del")
    @ApiOperation(value = "根据ID删除/批量删除角色", response = Response.class)
    public Response delRole(@ApiParam(name = "ids", value = "[角色id]")
                            @RequestBody SingleArray<String> ids){
        // 参数非空校验
        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }
        this.service.deleteRole(ids.getParam());
        return success(this.service.listRole(new RoleEntity(), 1));
    }

    @PostMapping("/lock")
    @ApiOperation(value = "锁定/解锁", response = Response.class)
    public Response lockSwitch(@ApiParam(name = "param", value = "角色[id]")
                               @RequestBody SingleArray<String> param) {
        // 参数非空校验
        if (null == param || CollectionUtil.isEmpty(param.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }

        this.service.updateRole(param.getParam(), param.getStatus());
        return success();
    }
}
