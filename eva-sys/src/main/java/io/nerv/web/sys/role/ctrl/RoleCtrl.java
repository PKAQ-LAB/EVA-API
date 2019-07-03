package io.nerv.web.sys.role.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.nerv.core.exception.ParamException;
import io.nerv.core.mvc.ctrl.PureBaseCtrl;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.mvc.util.SingleArray;
import io.nerv.web.sys.role.entity.RoleEntity;
import io.nerv.web.sys.role.entity.RoleModuleEntity;
import io.nerv.web.sys.role.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色管理
 * @author: S.PKAQ
 * @Datetime: 2018/4/13 7:27
 */
@Api( tags = "角色管理")
@RestController
@RequestMapping("/role")
public class RoleCtrl extends PureBaseCtrl<RoleService> {

    @PostMapping("/checkUnique")
    @ApiOperation(value = "校验角色编码唯一性",response = Response.class)
    public Response checkUnique(@ApiParam(name ="roleEntity", value = "要进行校验的参数")
                                @RequestBody RoleEntity role){
        boolean exist = this.service.checkUnique(role);
        return exist? failure(): success();
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "根据ID获取角色信息", response = Response.class)
    public Response getRole(@ApiParam(name = "id", value = "角色ID")
                            @PathVariable("id") String id){
        RoleEntity entity = this.service.getRole(id);
        return success(entity);
    }

    @GetMapping({"/list"})
    @ApiOperation(value = "获取角色列表", response = Response.class)
    public Response listRole(@ApiParam(name = "roleEntity", value = "包含角色对象属性的查询条件")
                             RoleEntity roleEntity, Integer page) {
        return success(this.service.listRole(roleEntity, page));
    }

    @GetMapping({"/listModule"})
    @ApiOperation(value = "获得角色绑定的菜单列表", response = Response.class)
    public Response listModule(@ApiParam(name = "roleEntity", value = "包含角色对象属性的查询条件")
                                RoleModuleEntity role) {
        return success(this.service.listModule(role));
    }

    @PostMapping({"/saveModule"})
    @ApiOperation(value = "保存角色模块关系", response = Response.class)
    public Response saveModule(@ApiParam(name = "param", value = "模块ID数组")
                               @RequestBody RoleEntity role) {
        this.service.saveModule(role);
        return success();
    }

    @GetMapping({"/listUser"})
    @ApiOperation(value = "获得角色绑定的用户列表", response = Response.class)
    public Response listUser(@ApiParam(name = "roleEntity", value = "包含角色对象属性的查询条件", required = true)
                             @RequestParam String roleId, Integer page) {
        return success(this.service.listUser(roleId, page));
    }

    @PostMapping({"/saveUser"})
    @ApiOperation(value = "保存角色用户关系", response = Response.class)
    public Response saveUser(@ApiParam(name = "param", value = "模块ID数组")
                             @RequestBody RoleEntity role) {
        if (null == role || StrUtil.isBlank(role.getId())){
            throw new ParamException("用户角色不允许为空");
        }
        this.service.saveUser(role);
        return success();
    }

    @PostMapping("/save")
    @ApiOperation(value = "新增/编辑角色信息", response = Response.class)
    public Response saveRole(@ApiParam(name ="role", value = "角色信息")
                             @RequestBody RoleEntity role){
        return success(this.service.saveRole(role));
    }

    @PostMapping("/del")
    @ApiOperation(value = "根据ID删除/批量删除角色", response = Response.class)
    @PreAuthorize("hasRole('ADMIN')")
    public Response delRole(@ApiParam(name = "ids", value = "[角色id]")
                            @RequestBody SingleArray<String> ids){
        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }
        this.service.deleteRole(ids.getParam());
        return success(this.service.listRole(new RoleEntity(), 1));
    }

    @PostMapping("/lock")
    @ApiOperation(value = "锁定/解锁", response = Response.class)
    public Response lockSwitch(@ApiParam(name = "params", value = "角色[id]")
                               @RequestBody SingleArray<String> params) {
        if (null == params || CollectionUtil.isEmpty(params.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }

        this.service.updateRole(params.getParam(), params.getStatus());
        return success();
    }
}
