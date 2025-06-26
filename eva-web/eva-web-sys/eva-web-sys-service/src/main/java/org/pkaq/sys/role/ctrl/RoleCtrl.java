package org.pkaq.sys.role.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.annotation.NoRepeatSubmit;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mvc.bo.Bo;
import org.pkaq.core.mvc.bo.PageBo;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.bo.StdBo;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.mybatis.mvc.ctrl.StdCtrl;
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
public class RoleCtrl extends StdCtrl<RoleService> {


    @GetMapping({"/listModule"})
    @Operation(summary = "获得角色绑定的菜单列表")
    public Response<Object> listModule(@Parameter(name = "role", description = "包含角色对象属性的查询条件")
                                       RoleModuleRefBo role) {
        return success(this.service.listModule(role));
    }

    @PostMapping({"/grantModule"})
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

    @PostMapping({"/grantUser"})
    @Operation(summary = "保存角色用户关系")
    public Response<Object> saveUser(@Parameter(name = "param", description = "角色用户id关系")
                                     @RequestBody @Valid RoleUserAoeBo role) {
        this.service.saveUser(role);
        return success();
    }

    @Override
    @PostMapping("/del")
    @Operation(summary = "根据ID删除/批量删除角色")
    public Response<Object> del(@Parameter(name = "ids", description = "[角色id]")
                                    @RequestBody SingleArray<Long> ids) {

        // 参数非空校验
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());

        this.service.delete(ids.getParam());
        return success();
    }


    @PostMapping("/edit")
    @Operation(summary = "新增记录", description = "新增/编辑记录")
    public Response<Object> save(@Parameter(name = "formdata", description = "模型对象")
                                 @RequestBody RoleAoeBo bo) {
        this.service.edit(bo);
        return success();
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询", description = "列表查询")
    public Response<Object> list(@Parameter(name = "page", description = "分页查询参数") RoleQueryBo page) {
        return this.success(this.service.listPage(page));
    }

}
