package io.nerv.web.sys.user.ctrl;

import io.nerv.security.exception.OathException;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.util.I18NHelper;
import io.nerv.security.util.SecurityUtil;
import io.nerv.web.sys.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private SecurityUtil securityUtil;


    @Value("${spring.profiles.active}")
    private String activeProfile;

    @GetMapping("/fetch")
    @ApiOperation(value = "获取当前登录用户的信息(菜单.权限.消息)",response = Response.class)
    public Response fetch() throws OathException {
        log.info("[auth/fetch ] - Current active profile is : " + activeProfile);
        final var id = securityUtil.getJwtUserId();

        return new Response().success(this.userService.fetch(id));
    }
}
