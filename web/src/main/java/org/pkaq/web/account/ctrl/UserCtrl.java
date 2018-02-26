package org.pkaq.web.account.ctrl;

import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.pkaq.core.enums.HttpCodeEnum;
import org.pkaq.core.util.Response;
import org.pkaq.web.account.entity.UserEntity;
import org.pkaq.web.account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户
 * Author: S.PKAQ
 * Datetime: 2018/2/8 18:59
 */
@RestController
@RequestMapping("/sys")
@Api(description = "用户账号接口")
public class UserCtrl {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @ApiOperation(value = "用户登陆", notes = "用户登陆后返回用户基本信息+权限信息",response = Response.class)
    public Response login(@ApiParam(value = "用户名") String username, @ApiParam("密码") String pwd) {
        Response response = null;
        if(StrUtil.isBlank(username) || StrUtil.isBlank(pwd)){
            response = new Response().failure(HttpCodeEnum.REQEUST_FAILURE.getIndex());
            return response;
        }

        response = new Response(true, "msg");
        return response;
    }

    @RequestMapping("/logout")
    @ApiOperation(value = "用户退出", notes = "用户退出登陆", response = Response.class)
    public Response logout(@ApiParam(value = "用户基本信息") UserEntity userEntity){
        return new Response().success();
    }
}
