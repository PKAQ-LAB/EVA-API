package org.pkaq.web.sys.user.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pkaq.core.exception.ParamException;
import org.pkaq.core.mvc.ctrl.BaseCtrl;
import org.pkaq.core.util.Response;
import org.pkaq.core.util.SingleArray;
import org.pkaq.web.sys.organization.entity.OrganizationEntity;
import org.pkaq.web.sys.user.entity.UserEntity;
import org.pkaq.web.sys.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理实体类
 *
 * @author: S.PKAQ
 * @Datetime: 2018/3/30 0:03
 */
@Api(description = "用户管理")
@RestController
@RequestMapping("/account")
public class UserCtrl extends BaseCtrl<UserService> {

    @GetMapping({"/list"})
    @ApiOperation(value = "获取用户列表", response = Response.class)
    public Response listUser(@ApiParam(name = "userEntity", value = "包含用户对象属性的查询条件")
                                        UserEntity userEntity) {
        return success(this.service.listUser(userEntity));
    }

    @PostMapping("/del")
    @ApiOperation(value = "根据ID删除/批量删除用户", response = Response.class)
    public Response delUser(@ApiParam(name = "ids", value = "[用户id]")
                           @RequestBody SingleArray<String> ids){
        // 参数非空校验
        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }
        this.service.deleteUser(ids.getParam());
        return success(this.service.listUser(new UserEntity()));
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
        return success();
    }
}
