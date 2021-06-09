package io.nerv.web.sys.role.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.ParamException;
import io.nerv.core.mvc.ctrl.Ctrl;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.mvc.vo.SingleArray;
import io.nerv.web.sys.role.entity.RoleEntity;
import io.nerv.web.sys.role.entity.RoleModuleEntity;
import io.nerv.web.sys.role.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 角色管理
 * @author: S.PKAQ
 */
@Api( tags = "角色管理")
@RestController
@RequestMapping("/sys/role")
@RequiredArgsConstructor
public class RoleCtrl extends Ctrl {
    private final RoleService service;

    @PostMapping("/checkUnique")
    @ApiOperation(value = "校验角色编码唯一性",response = Response.class)
    public Response checkUnique(@ApiParam(name ="roleEsaveModulentity", value = "要进行校验的参数")
                                @RequestBody RoleEntity role){
        boolean exist = this.service.checkUnique(role);
        return exist? failure(BizCodeEnum.ROLE_CODE_EXIST): success();
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
    public Response listRoles(@ApiParam(name = "roleEntity", value = "包含角色对象属性的查询条件")
                             RoleEntity roleEntity, Integer page, Integer pageSize) {
        return success(this.service.listRole(roleEntity, page, pageSize));
    }

    @GetMapping({"/listAll"})
    @ApiOperation(value = "获取角色列表 - 无分页", response = Response.class)
    public Response listAllRoles(@ApiParam(name = "roleEntity", value = "包含角色对象属性的查询条件")
                                     RoleEntity roleEntity) {
        return success(this.service.listRole(roleEntity));
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
                             @RequestParam String roleId,
                             @RequestParam(required = false) String deptId) {
        return success(this.service.listUser(roleId, deptId));
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
        this.service.saveRole(role);
        return success();
    }

    @PostMapping("/del")
    @ApiOperation(value = "根据ID删除/批量删除角色", response = Response.class)
    public Response delRole(@ApiParam(name = "ids", value = "[角色id]")
                            @RequestBody SingleArray<String> ids){
        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }
        this.service.deleteRole(ids.getParam());
        return success();
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
