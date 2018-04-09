package org.pkaq.web.sys.user.ctrl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pkaq.core.mvc.ctrl.BaseCtrl;
import org.pkaq.core.util.Response;
import org.pkaq.web.sys.user.entity.UserEntity;
import org.pkaq.web.sys.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public Response listOrg(@ApiParam(name = "userEntity", value = "包含用户对象属性的查询条件")
                                        UserEntity userEntity) {
        return success(this.service.listUser(userEntity));
    }
}
