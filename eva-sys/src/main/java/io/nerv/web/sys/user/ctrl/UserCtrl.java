package io.nerv.web.sys.user.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.ParamException;
import io.nerv.core.mvc.ctrl.mybatis.PureBaseCtrl;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.mvc.util.SingleArray;
import io.nerv.web.sys.user.entity.UserEntity;
import io.nerv.web.sys.user.service.UserService;
import io.nerv.web.sys.user.vo.UserCenterVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理实体类
 *
 * @author: S.PKAQ
 * @Datetime: 2018/3/30 0:03
 */
@Api("用户管理")
@RestController
@RequestMapping("/account")
public class UserCtrl extends PureBaseCtrl<UserService> {

    @PostMapping("/checkUnique")
    @ApiOperation(value = "校验账号唯一性",response = Response.class)
    public Response checkUnique(@ApiParam(name ="userEntity", value = "要进行校验的参数")
                                @RequestBody UserEntity user){
        boolean exist = this.service.checkUnique(user);
        return exist? failure(BizCodeEnum.ACCOUNT_ALREADY_EXIST): success();
    }

    @PostMapping("/del")
    @ApiOperation(value = "根据ID删除/批量删除记录",response = Response.class)
    public Response del(@ApiParam(name = "ids", value = "[记录ID]")
                        @RequestBody SingleArray<String> ids){

        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }
        this.service.delete(ids.getParam());
        return success(this.service.listPage(null, 1));
    }

    @PostMapping("/edit")
    @ApiOperation(value = "新增/编辑记录",response = Response.class)
    public Response save(@ApiParam(name ="formdata", value = "用户对象")
                         @RequestBody UserEntity entity){

        return success(this.service.saveUser(entity));
    }

    @GetMapping("/list")
    @ApiOperation(value = "列表查询",response = Response.class)
    public Response list(@ApiParam(name ="condition", value = "用户对象")
                                     UserEntity entity, Integer pageNo){
        return this.success(this.service.listPage(entity, pageNo));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "根据ID获得记录信息", response = Response.class)
    public Response getRole(@ApiParam(name = "id", value = "记录ID")
                            @PathVariable("id") String id){
        UserCenterVO userCenterVO = this.service.getUser(id);
        return null == userCenterVO? this.failure() : this.success(userCenterVO, BizCodeEnum.SAVE_SUCCESS);
    }

    @PostMapping("/lock")
    @ApiOperation(value = "锁定/解锁", response = Response.class)
    public Response lockSwitch(@ApiParam(name = "param", value = "用户[id]")
                               @RequestBody SingleArray<String> param) {
        // 参数非空校验
        if (null == param || CollectionUtil.isEmpty(param.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }

        this.service.updateUser(param.getParam(), param.getStatus());
        return success(this.service.listPage(null, 1));
    }
}
