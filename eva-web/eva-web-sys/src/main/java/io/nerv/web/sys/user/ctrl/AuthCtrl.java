package io.nerv.web.sys.user.ctrl;

import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.threaduser.ThreadUserHelper;
import io.nerv.core.util.I18NHelper;
import io.nerv.web.sys.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWT鉴权
 * @author: S.PKAQ
 * @Datetime: 2018/4/22 17:12
 */
@Slf4j
@Tag(name = "用户信息")
@RestController
@RequestMapping("/auth")
public class AuthCtrl {
    @Autowired
    private UserService userService;

    @Autowired
    protected I18NHelper i18NHelper;

    @GetMapping("/fetch")
    @Operation(summary = "获取当前登录用户的信息(菜单.权限.消息)")
    public Response fetch()  {

        try {
            final var userId = ThreadUserHelper.getUserId();

            return new Response().success(this.userService.fetch(userId));
        } catch (Exception e) {
            return new Response().failure(BizCodeEnum.SERVER_ERROR);
        }
    }
}
