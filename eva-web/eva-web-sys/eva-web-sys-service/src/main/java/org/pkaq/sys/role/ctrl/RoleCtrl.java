package org.pkaq.sys.role.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.mybatis.mvc.ctrl.StdCtrl;
import org.pkaq.sys.role.bo.RoleAoeBo;
import org.pkaq.sys.role.bo.RoleQueryBo;
import org.pkaq.sys.role.bo.RoleResourceRefBo;
import org.pkaq.sys.role.bo.RoleUserRefBo;
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

    @GetMapping({"/fetchResource"})
    @Operation(summary = "获得角色绑定的菜单资源列表")
    @BizLog(operateType = BizLogCodes.EDIT, description = "查询了角色授权资源", args = {"param:0"})
    public Response<Object> fetchResource(@Parameter(name = "role", description = "包含角色对象属性的查询条件")
                                          RoleResourceRefBo role) {
        return success(this.service.fetchResource(role));
    }

    @PostMapping({"/grantResource"})
    @Operation(summary = "保存角色模块关系")
    @BizLog(operateType = BizLogCodes.EDIT, description = "更新了角色资源授权[{0}]", args = {"param:0"})
    public Response<Object> grantResource(@Parameter(name = "param", description = "角色详情")
                                          @RequestBody RoleResourceRefBo roleAoeBo) {
        this.service.grantResource(roleAoeBo);
        return success();
    }

    @GetMapping({"/listUser"})
    @Operation(summary = "获得角色绑定的用户列表")
    @BizLog(operateType = BizLogCodes.EDIT, description = "查询了角色授权用户[{0}]", args = {"param:0"})
    public Response<Object> listUser(@Parameter(name = "roleEntity", description = "包含角色对象属性的查询条件", required = true)
                                     @RequestParam Long roleId,
                                     @RequestParam(required = false) Long deptId) {
        return success(this.service.listUser(roleId, deptId));
    }

    @PostMapping({"/grantUser"})
    @Operation(summary = "保存角色用户关系")
    @BizLog(operateType = BizLogCodes.EDIT, description = "更新了橘色授权用户[{0}]", args = {"param:0"})
    public Response<Object> grantUser(@Parameter(name = "param", description = "角色用户id关系")
                                      @RequestBody @Valid RoleUserRefBo role) {
        this.service.grantUser(role);
        return success();
    }

    @Override
    @PostMapping("/del")
    @Operation(summary = "根据ID删除/批量删除角色")
    @BizLog(operateType = BizLogCodes.EDIT, description = "删除了角色[{0}]", args = {"param:0"})
    public Response<Object> del(@Parameter(name = "ids", description = "[角色id]")
                                @RequestBody SingleArray<Long> ids) {

        // 参数非空校验
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());

        this.service.delete(ids.getParam());
        return success();
    }

    @PostMapping("/edit")
    @Operation(summary = "新增记录", description = "新增/编辑记录")
    @BizLog(operateType = BizLogCodes.EDIT, description = "编辑了角色信息[{0}]", args = {"param:0"})
    public Response<Object> save(@Parameter(name = "formdata", description = "模型对象")
                                 @RequestBody RoleAoeBo bo) {
        this.service.edit(bo);
        return success();
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询", description = "列表查询")
    @BizLog(operateType = BizLogCodes.EDIT, description = "查询了角色列表[{0}]", args = {"param:0"})
    public Response<Object> list(RoleQueryBo page) {
        return this.success(this.service.listPage(page));
    }
}
