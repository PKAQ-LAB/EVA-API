package io.nerv.web.sys.user.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.ctrl.Ctrl;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.mvc.vo.SingleArray;
import io.nerv.web.sys.user.entity.UserEntity;
import io.nerv.web.sys.user.service.UserService;
import io.nerv.web.sys.user.vo.PasswordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理实体类
 *
 * @author: S.PKAQ
 * @Datetime: 2018/3/30 0:03
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/sys/account")
@RequiredArgsConstructor
public class UserCtrl extends Ctrl {
    private final UserService service;

    @PostMapping("/checkUnique")
    @Operation(summary = "校验账号唯一性")
    public Response checkUnique(@Parameter(name ="userEntity", description = "要进行校验的参数")
                                @RequestBody UserEntity user){
        boolean exist = (null != user && StrUtil.isNotBlank(user.getAccount()))? this.service.checkUnique(user) : false;
        return exist? failure(BizCodeEnum.ACCOUNT_ALREADY_EXIST): success();
    }

    @PostMapping("repwd")
    @Operation(summary ="重新设置密码")
    public Response repwd(@Parameter(name ="formdata", description = "用户对象")
                          @RequestBody PasswordVO passwordVO){
        return this.service.repwd(passwordVO)? success("", BizCodeEnum.OPERATE_SUCCESS): failure(BizCodeEnum.BAD_ORG_PASSWORD);
    }

    @PostMapping("/del")
    @Operation(summary ="根据ID删除/批量删除记录")
    public Response del(@Parameter(name = "ids", description = "[记录ID]")
                        @RequestBody SingleArray<String> ids){

        // 参数非空校验
        BizCodeEnum.NULL_ID.assertNotNull(ids);
        BizCodeEnum.NULL_ID.assertNotNull(ids.getParam());

        this.service.delete(ids.getParam());
        return success(null, BizCodeEnum.OPERATE_SUCCESS);
    }

    @PostMapping("/edit")
    @Operation(summary ="新增/编辑记录")
    public Response save(@Parameter(name ="formdata", description = "用户对象")
                         @RequestBody UserEntity entity){
        this.service.saveUser(entity);
        return success(null, BizCodeEnum.OPERATE_SUCCESS);
    }

    @PostMapping("/grant")
    @Operation(summary ="授权")
    public Response grant(@Parameter(name ="formdata", description = "用户对象")
                         @RequestBody UserEntity entity){

        this.service.saveRoles(entity);
        return success(null, BizCodeEnum.SAVE_SUCCESS);
    }

    @GetMapping("/list")
    @Operation(summary ="列表查询")
    public Response list(@Parameter(name ="condition", description = "用户对象")
                                   UserEntity entity, Integer pageNo, Integer pageSize){
        return success(this.service.listUser(entity, pageNo, pageSize));
    }

    @GetMapping("/get/{id}")
    @Operation(summary ="根据ID获得记录信息")
    public Response getRole(@Parameter(name = "id", description = "记录ID")
                            @PathVariable("id") String id){
        UserEntity user = this.service.getUser(id);
        return null == user? this.failure() : this.success(user);
    }

    @PostMapping("/lock")
    @Operation(summary ="锁定/解锁")
    public Response lockSwitch(@Parameter(name = "param", description = "用户[id]")
                               @RequestBody SingleArray<String> param) {
        // 参数非空校验
        BizCodeEnum.NULL_ID.assertNotNull(param);
        BizCodeEnum.NULL_ID.assertNotNull(param.getParam());

        this.service.updateUser(param.getParam(), param.getStatus());
        return success(this.service.listPage(null, 1));
    }
}
