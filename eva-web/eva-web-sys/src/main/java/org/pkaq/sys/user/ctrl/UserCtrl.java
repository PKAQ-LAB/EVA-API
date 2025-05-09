package org.pkaq.sys.user.ctrl;

import cn.hutool.core.text.CharSequenceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.enums.BizCodeEnum;
import org.pkaq.core.mvc.bo.SingleArrayBo;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.sys.SysCodeEnum;
import org.pkaq.sys.user.bo.RePwdBo;
import org.pkaq.sys.user.bo.UserAoeBo;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理实体类
 * @author: S.PKAQ
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/sys/account")
@RequiredArgsConstructor
public class UserCtrl extends Ctrl {
    private final UserService service;

    @PostMapping("/checkUnique")
    @Operation(summary = "校验账号唯一性")
    public Response<Object> checkUnique(@Parameter(name = "UserAoeBo", description = "要进行校验的参数")
                                        @RequestBody UserAoeBo bo) {
        boolean exist = null != bo && CharSequenceUtil.isNotBlank(bo.getAccount()) && this.service.checkUnique(bo);
        return exist ? failure(SysCodeEnum.ACCOUNT_ALREADY_EXIST) : success();
    }

    @PostMapping("repwd")
    @Operation(summary = "重新设置密码")
    public Response<Object> repwd(@Parameter(name = "formdata", description = "用户对象")
                                  @RequestBody RePwdBo rePwdBo) {
        return this.service.repwd(rePwdBo) ? success() : failure(BizCodeEnum.BAD_ORG_PASSWORD);
    }

    @PostMapping("/del")
    @Operation(summary = "根据ID删除/批量删除记录")
    public Response<Object> del(@Parameter(name = "ids", description = "[记录ID]")
                                @RequestBody @Valid SingleArrayBo<String> ids) {

        // 参数非空校验
        BizCodeEnum.NULL_ID.assertNotNull(ids);
        BizCodeEnum.NULL_ID.assertNotNull(ids.getParam());

        this.service.delete(ids.getParam());
        return success();
    }

    @PostMapping("/edit")
    @Operation(summary = "新增/编辑记录")
    public Response<Object> save(@Parameter(name = "formdata", description = "用户对象")
                                 @RequestBody UserAoeBo bo) {
        this.service.saveUser(bo);
        return success();
    }

    @PostMapping("/grant")
    @Operation(summary = "授权")
    public Response<Object> grant(@Parameter(name = "formdata", description = "用户对象")
                                  @RequestBody UserAoeBo bo) {
        this.service.saveRoles(bo);
        return success(null, BizCodeEnum.SAVE_SUCCESS);
    }

    @GetMapping("/list")
    @Operation(summary = "列表查询")
    public Response<Object> list(@Parameter(name = "condition", description = "用户对象")
                                 UserAoeBo bo, Integer pageNo, Integer pageSize) {
        return success(this.service.listUser(bo, pageNo, pageSize));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID获得记录信息")
    public Response<Object> getRole(@Parameter(name = "id", description = "记录ID")
                                    @PathVariable("id") String id) {
        UserEntity user = this.service.getUser(id);
        return null == user ? this.failure() : this.success(user);
    }

    @PostMapping("/lock")
    @Operation(summary = "锁定/解锁")
    public Response<Object> lockSwitch(@Parameter(name = "param", description = "用户[id]")
                                       @RequestBody SingleArrayBo<String> param) {
        // 参数非空校验
        BizCodeEnum.NULL_ID.assertNotNull(param);
        BizCodeEnum.NULL_ID.assertNotNull(param.getParam());

        this.service.updateUser(param.getParam(), param.getStatus());
        return success(this.service.listPage(null, 1));
    }
}
