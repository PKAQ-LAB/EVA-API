package org.pkaq.web.sys.user.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pkaq.core.enums.HttpCodeEnum;
import org.pkaq.core.mvc.util.Response;
import org.pkaq.core.util.I18NHelper;
import org.pkaq.security.jwt.JwtConfig;
import org.pkaq.security.jwt.JwtUtil;
import org.pkaq.web.sys.module.entity.ModuleEntity;
import org.pkaq.web.sys.user.entity.UserEntity;
import org.pkaq.web.sys.user.service.UserService;
import org.pkaq.util.TreeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT鉴权
 * @author: S.PKAQ
 * @Datetime: 2018/4/22 17:12
 */
@Api(description = "用户登录")
@RestController
@RequestMapping("/auth")
public class AuthCtrl {
    @Autowired
    private UserService userService;
    @Autowired
    protected I18NHelper i18NHelper;
    @Autowired
    private JwtConfig jwtConfig;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    @ApiOperation(value = "用户登录", response = Response.class)
    public Response login(@ApiParam(name = "{user}", value = "用户对象")
                          @RequestBody UserEntity user){
        user =this.userService.validate(user);
        Response response;
        if(null == user){
            response = new Response().failure(HttpCodeEnum.ROLE_ERROR.getIndex(), i18NHelper.getMessage("login_failed"));
        } else if(CollectionUtil.isEmpty(user.getModules()) || CollectionUtil.isEmpty(user.getRoles())){
            response = new Response().failure(HttpCodeEnum.ROLE_ERROR.getIndex(), i18NHelper.getMessage("permission_denied"));
        }else {
            // 签发token
            String token = jwtUtil.build(jwtConfig.getTtl(), user.getId());
            // 整理菜单
            List<ModuleEntity> treeModule = new TreeHelper().bulid(user.getModules());

            user.setModules(treeModule);

            Map<String, Object> map = new HashMap<>(2);
            map.put("token", token);
            map.put("user", user);
            response = new Response().success(map);
        }
        return response;
    }

    @PostMapping("/logout")
    @ApiOperation(value = "用户退出", response = Response.class)
    public Response logout(@ApiParam(name = "{user}", value = "用户对象")
                                 @RequestBody UserEntity user){
        // 获取用户jwt
        // 清空redis中的jwt 刷新用户secret
        return new Response().success();
    }
}