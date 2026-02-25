package org.pkaq.sys.user.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.util.StrUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.role.service.UserRoleRefSerivce;
import org.pkaq.sys.user.bo.*;
import org.pkaq.sys.user.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理实体类
 *
 * @author PKAQ
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/sys/account")
@RequiredArgsConstructor
public class UserCtrl extends Ctrl {
    private final UserService service;

    private final UserRoleRefSerivce userRoleRefSerivce;

    @PostMapping("/checkUnique")
    @Operation(summary = "校验账号唯一性")
    public Response<Object> checkUnique(@Parameter(name = "bo", description = "要进行校验的参数")
                                        @RequestBody @Valid UserCheckBo bo) {
        if (StrUtils.isBlank(bo.getAccount()) && StrUtils.isBlank(bo.getCode())) {
            SysCodes.CHECKFIELD_ALREADY_EXIST.newException();
        }
        boolean exist = this.service.checkUnique(bo);

        if (exist) {
            if (StrUtils.isBlank(bo.getAccount())) {
                return failure(SysCodes.CODE_ALREADY_EXIST);
            } else if (StrUtils.isBlank(bo.getCode())) {
                return failure(SysCodes.ACCOUNT_ALREADY_EXIST);
            } else {
                return failure(SysCodes.ACCOUNT_OR_CODE_ALREADY_EXIST);
            }
        }

        return success();
    }

    @PostMapping("repwd")
    @Operation(summary = "重新设置密码")
    @BizLog(operateType = BizLogCodes.EDIT, description = "重新设置了密码[{0}]", args = {"param:0"})
    public Response<Object> repwd(@Parameter(name = "rePwdBo", description = "用户对象")
                                  @RequestBody RePwdBo rePwdBo) {
        this.service.repwd(rePwdBo);
        return success();
    }

    @PostMapping("/del")
    @Operation(summary = "根据ID删除/批量删除记录")
    @BizLog(operateType = BizLogCodes.EDIT, description = "删除了用户[{0}]", args = {"param:0.params"})
    public Response<Object> del(@Parameter(name = "ids", description = "[记录ID]")
                                @RequestBody @Valid SingleArray<Long> ids) {
        // 参数非空校验
        CommonCodes.NULL_ID.assertNotNull(ids);

        this.service.delete(ids.getParam());
        return success();
    }

    @PostMapping("/edit")
    @Operation(summary = "新增/编辑记录")
    @BizLog(operateType = BizLogCodes.EDIT, description = "编辑了用户信息[{0}]", args = {"param:0.id"})
    public Response<Object> edit(@Parameter(name = "UserAoeBo", description = "用户对象")
                                 @RequestBody UserAoeBo bo) {
        this.service.saveUser(bo);
        return success();
    }

    @PostMapping("/grant")
    @Operation(summary = "授权")
    @BizLog(operateType = BizLogCodes.EDIT, description = "重新授权了用户权限[{0}]", args = {"param:0.id"})
    public Response<Object> grant(@Parameter(name = "bo", description = "授权角色")
                                  @RequestBody UserGrantBo bo) {
        this.userRoleRefSerivce.saveRoles(bo);
        return success(null, CommonCodes.SAVE_SUCCESS);
    }

    @GetMapping("/list")
    @Operation(summary = "列表查询")
    @BizLog(operateType = BizLogCodes.EDIT, description = "查询了用户列表[{0}]", args = {"param:0.id"})
    public Response<Object> list(@Parameter(name = "condition", description = "用户对象")
                                 UserQueryBo bo) {
        return success(this.service.listPage(bo));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID获得记录信息")
    @BizLog(operateType = BizLogCodes.EDIT, description = "查询了用户信息[{0}]", args = {"param:0.id"})
    public Response<Object> get(@Parameter(name = "id", description = "记录ID")
                                @PathVariable("id") Long id) {
        return this.success(this.service.getUser(id));
    }

    @PostMapping("/switch")
    @Operation(summary = "锁定/解锁")
    @BizLog(operateType = BizLogCodes.EDIT, description = "锁定/解锁了用户[{0}]", args = {"param:0.status"})
    public Response<Object> change(@Parameter(name = "param", description = "用户[id]")
                                   @RequestBody SingleArray<Long> param) {
        // 参数非空校验
        CommonCodes.NULL_ID.assertNotNull(param.getParam());

        this.service.updateUser(param.getParam());
        return success();
    }
}
