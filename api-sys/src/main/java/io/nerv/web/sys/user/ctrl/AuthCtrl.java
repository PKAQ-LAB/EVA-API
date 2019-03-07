package io.nerv.web.sys.user.ctrl;

import io.nerv.core.enums.HttpCodeEnum;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.util.I18NHelper;
import io.nerv.security.jwt.JwtConfig;
import io.nerv.security.jwt.JwtUtil;
import io.nerv.web.sys.user.entity.UserEntity;
import io.nerv.web.sys.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
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
    @Autowired
    private HttpServletRequest httpRequest;

    @PostMapping("/login")
    @ApiOperation(value = "用户登录", response = Response.class)
    public Response login(@ApiParam(name = "{user}", value = "用户对象")
                          @RequestBody UserEntity user){
        user =this.userService.validate(user);
        Response response;
        if(null == user){
            response = new Response().failure(HttpCodeEnum.ROLE_ERROR.getIndex(), i18NHelper.getMessage("login_failed"));
        } else {
            // 签发token
            String token = jwtUtil.build(jwtConfig.getTtl(), user.getId());

            Map<String, Object> map = new HashMap<>(1);
            map.put("user", user);
            map.put("token", token);
            response = new Response().success(map, "登录成功");
        }
        return response;
    }

    @GetMapping("/fetch")
    @ApiOperation(value = "获取当前登录用户的信息(菜单.权限.消息)",response = Response.class)
    public Response fetch(){
        String authHeader = httpRequest.getHeader(jwtConfig.getHeader());

        final String authToken = authHeader.substring(jwtConfig.getTokenHead().length());
        String account = jwtUtil.getUid(authToken);

        return new Response().success(this.userService.fetch(account));
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