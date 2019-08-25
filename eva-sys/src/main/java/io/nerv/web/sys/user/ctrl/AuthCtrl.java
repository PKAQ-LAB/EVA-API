package io.nerv.web.sys.user.ctrl;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import io.nerv.core.constant.TokenConst;
import io.nerv.core.exception.OathException;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.util.I18NHelper;
import io.nerv.properties.EvaConfig;
import io.nerv.security.jwt.JwtUtil;
import io.nerv.security.util.SecurityUtil;
import io.nerv.web.sys.user.entity.UserEntity;
import io.nerv.web.sys.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT鉴权
 * @author: S.PKAQ
 * @Datetime: 2018/4/22 17:12
 */
@Slf4j
@Api("用户登录")
@RestController
@RequestMapping("/auth")
public class AuthCtrl {
    @Autowired
    private UserService userService;

    @Autowired
    protected I18NHelper i18NHelper;

    @Autowired
    private EvaConfig evaConfig;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private HttpServletResponse response;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @PostMapping("/login")
    @ApiOperation(value = "用户登录", response = Response.class)
    public Response login(@ApiParam(name = "{user}", value = "用户对象")
                          @RequestBody UserEntity user) throws OathException, UnsupportedEncodingException {
        user =this.userService.validate(user);
        // 签发token
        String token = jwtUtil.build(evaConfig.getJwt().getTtl(), user.getAccount());

        ServletUtil.addCookie(response,
                              TokenConst.TOKEN_KEY,
                              token,
                              evaConfig.getCookie().getMaxAge(),
                             "/",
                              evaConfig.getCookie().getDomain());


        ServletUtil.addCookie(response,
                TokenConst.USER_KEY,
                URLEncoder.encode(JSON.toJSONString(user), CharsetUtil.UTF_8),
                evaConfig.getCookie().getMaxAge(),
                "/",
                evaConfig.getCookie().getDomain());

        Map<String, Object> map = new HashMap<>(2);
        map.put("user", user);
        map.put("token", token);

       return new Response().success( map, "登录成功");
    }

    @GetMapping("/fetch")
    @ApiOperation(value = "获取当前登录用户的信息(菜单.权限.消息)",response = Response.class)
    public Response fetch() throws OathException {
        log.info("[auth/fetch ] - Current active profile is : " + activeProfile);
        // 开发环境不鉴权直接取admin菜单
        final String account = securityUtil.getJwtUserId();

        return new Response().success(this.userService.fetch(account));
    }

    @PostMapping("/logout")
    @ApiOperation(value = "用户退出", response = Response.class)
    public Response logout(@ApiParam(name = "{user}", value = "用户对象")
                                 @RequestBody UserEntity user){
        // 获取用户jwt
        // 清空redis中的jwt 刷新用户secret
        ServletUtil.addCookie(response,
                TokenConst.TOKEN_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());


        ServletUtil.addCookie(response,
                TokenConst.USER_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());
        return new Response().success();
    }
}
