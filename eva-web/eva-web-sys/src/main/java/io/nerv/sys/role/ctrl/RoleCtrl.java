package io.nerv.sys.role.ctrl;

import cn.hutool.core.util.StrUtil;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.bo.SingleArrayBo;
import io.nerv.core.mvc.ctrl.Ctrl;
import io.nerv.core.mvc.response.Response;
import io.nerv.sys.role.entity.RoleEntity;
import io.nerv.sys.role.entity.RoleModuleEntity;
import io.nerv.sys.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 角色管理
 *
 * @author: S.PKAQ
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/sys/role")
@RequiredArgsConstructor
public class RoleCtrl extends Ctrl {
    private final RoleService service;

    @PostMapping("/checkUnique")
    @Operation(summary = "校验角色编码唯一性")
    public Response checkUnique(@Parameter(name = "roleEsaveModulentity", description = "要进行校验的参数")
                                @RequestBody RoleEntity role) {
        boolean exist = (null != role && StrUtil.isNotBlank(role.getCode())) ? this.service.checkUnique(role) : false;
        return exist ? failure(BizCodeEnum.ROLE_CODE_EXIST) : success();
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID获取角色信息")
    public Response getRole(@Parameter(name = "id", description = "角色ID")
                            @PathVariable("id") String id) {
        RoleEntity entity = this.service.getRole(id);
        return success(entity);
    }

    @GetMapping({"/list"})
    @Operation(summary = "获取角色列表")
    public Response listRoles(@Parameter(name = "roleEntity", description = "包含角色对象属性的查询条件")
                              RoleEntity roleEntity, Integer page, Integer pageSize) {
        return success(this.service.listRole(roleEntity, page, pageSize));
    }

    @GetMapping({"/listAll"})
    @Operation(summary = "获取角色列表 - 无分页")
    public Response listAllRoles(@Parameter(name = "roleEntity", description = "包含角色对象属性的查询条件")
                                 RoleEntity roleEntity) {
        return success(this.service.listRole(roleEntity));
    }

    @GetMapping({"/listModule"})
    @Operation(summary = "获得角色绑定的菜单列表")
    public Response listModule(@Parameter(name = "roleEntity", description = "包含角色对象属性的查询条件")
                               RoleModuleEntity role) {
        return success(this.service.listModule(role));
    }

    @PostMapping({"/saveModule"})
    @Operation(summary = "保存角色模块关系")
    public Response saveModule(@Parameter(name = "param", description = "模块ID数组")
                               @RequestBody RoleEntity role) {
        this.service.saveModule(role);
        return success();
    }

    @GetMapping({"/listUser"})
    @Operation(summary = "获得角色绑定的用户列表")
    public Response listUser(@Parameter(name = "roleEntity", description = "包含角色对象属性的查询条件", required = true)
                             @RequestParam String roleId,
                             @RequestParam(required = false) String deptId) {
        return success(this.service.listUser(roleId, deptId));
    }

    @PostMapping({"/saveUser"})
    @Operation(summary = "保存角色用户关系")
    public Response saveUser(@Parameter(name = "param", description = "模块ID数组")
                             @RequestBody RoleEntity role) {
        BizCodeEnum.NULL_PARAM_ID.assertNotNull(role, "角色");
        BizCodeEnum.NULL_PARAM_ID.assertNotNull(role.getId(), "角色");

        this.service.saveUser(role);
        return success();
    }

    @PostMapping("/save")
    @Operation(summary = "新增/编辑角色信息")
    public Response saveRole(@Parameter(name = "role", description = "角色信息")
                             @RequestBody RoleEntity role) {
        this.service.saveRole(role);
        return success();
    }

    @PostMapping("/del")
    @Operation(summary = "根据ID删除/批量删除角色")
    public Response delRole(@Parameter(name = "ids", description = "[角色id]")
                            @RequestBody SingleArrayBo<String> ids) {

        // 参数非空校验
        BizCodeEnum.NULL_ID.assertNotNull(ids);
        BizCodeEnum.NULL_ID.assertNotNull(ids.getParam());

        this.service.deleteRole(ids.getParam());
        return success();
    }

    @PostMapping("/lock")
    @Operation(summary = "锁定/解锁")
    public Response lockSwitch(@Parameter(name = "params", description = "角色[id]")
                               @RequestBody SingleArrayBo<String> params) {

        // 参数非空校验
        BizCodeEnum.NULL_ID.assertNotNull(params);
        BizCodeEnum.NULL_ID.assertNotNull(params.getParam());

        this.service.updateRole(params.getParam(), params.getStatus());
        return success();
    }
}
