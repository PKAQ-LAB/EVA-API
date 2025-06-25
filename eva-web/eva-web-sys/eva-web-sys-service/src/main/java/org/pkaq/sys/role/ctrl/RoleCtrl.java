package org.pkaq.sys.role.ctrl;

import cn.hutool.core.text.CharSequenceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mvc.bo.SingleArrayBo;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.role.bo.RoleAoeBo;
import org.pkaq.sys.role.bo.RoleModuleRefBo;
import org.pkaq.sys.role.bo.RoleQueryBo;
import org.pkaq.sys.role.bo.RoleUserAoeBo;
import org.pkaq.sys.role.service.RoleService;
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
    public Response<Object> checkUnique(@Parameter(name = "roleEsaveModulentity", description = "要进行校验的参数")
                                        @RequestBody RoleAoeBo role) {
        boolean exist = null != role && CharSequenceUtil.isNotBlank(role.getCode()) && this.service.checkUnique(role);
        return exist ? failure(SysCodes.ROLE_CODE_EXIST) : success();
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID获取角色信息")
    public Response<Object> getRole(@Parameter(name = "id", description = "角色ID")
                                    @PathVariable("id") String id) {
        return success(this.service.getRole(id));
    }

    @GetMapping({"/list"})
    @Operation(summary = "获取角色列表")
    public Response<Object> listRoles(@Parameter(name = "queryBo", description = "包含角色对象属性的查询条件")
                                      RoleQueryBo queryBo, Integer page, Integer pageSize) {
        return success(this.service.listRole(queryBo, page, pageSize));
    }

    @GetMapping({"/listAll"})
    @Operation(summary = "this.service.getRole(id) - 无分页")
    public Response<Object> listAllRoles(@Parameter(name = "queryBo", description = "包含角色对象属性的查询条件")
                                 RoleQueryBo queryBo) {
        return success(this.service.listRole(queryBo));
    }

    @GetMapping({"/listModule"})
    @Operation(summary = "获得角色绑定的菜单列表")
    public Response<Object> listModule(@Parameter(name = "role", description = "包含角色对象属性的查询条件")
                                       RoleModuleRefBo role) {
        return success(this.service.listModule(role));
    }

    @PostMapping({"/saveModule"})
    @Operation(summary = "保存角色模块关系")
    public Response<Object> saveModule(@Parameter(name = "param", description = "角色详情")
                                       @RequestBody RoleAoeBo roleAoeBo) {
        this.service.saveModule(roleAoeBo);
        return success();
    }

    @GetMapping({"/listUser"})
    @Operation(summary = "获得角色绑定的用户列表")
    public Response<Object> listUser(@Parameter(name = "roleEntity", description = "包含角色对象属性的查询条件", required = true)
                             @RequestParam Long roleId,
                             @RequestParam(required = false) Long deptId) {
        return success(this.service.listUser(roleId, deptId));
    }

    @PostMapping({"/saveUser"})
    @Operation(summary = "保存角色用户关系")
    public Response<Object> saveUser(@Parameter(name = "param", description = "角色用户id关系")
                                     @RequestBody @Valid RoleUserAoeBo role) {
        this.service.saveUser(role);
        return success();
    }

    @PostMapping("/save")
    @Operation(summary = "新增/编辑角色信息")
    public Response<Object> saveRole(@Parameter(name = "role", description = "角色信息")
                                     @RequestBody @Valid RoleAoeBo role) {
        this.service.saveRole(role);
        return success();
    }

    @PostMapping("/del")
    @Operation(summary = "根据ID删除/批量删除角色")
    public Response<Object> delRole(@Parameter(name = "ids", description = "[角色id]")
                                    @RequestBody SingleArrayBo<String> ids) {

        // 参数非空校验
        CommonCodes.NULL_ID.assertNotNull(ids);
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());

        this.service.deleteRole(ids.getParam());
        return success();
    }

    @PostMapping("/lock")
    @Operation(summary = "锁定/解锁")
    public Response<Object> lockSwitch(@Parameter(name = "params", description = "角色[id]")
                                       @RequestBody SingleArrayBo<String> params) {

        // 参数非空校验
        CommonCodes.NULL_ID.assertNotNull(params);
        CommonCodes.NULL_ID.assertNotNull(params.getParam());

//        this.service.updateRole(params.getParam(), params.getParam());
        return success();
    }
}
