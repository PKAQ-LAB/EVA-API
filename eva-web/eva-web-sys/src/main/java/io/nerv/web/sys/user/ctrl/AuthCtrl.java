package io.nerv.web.sys.user.ctrl;

import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.threaduser.ThreadUserHelper;
import io.nerv.core.util.I18NHelper;
import io.nerv.web.sys.dict.cache.DictCacheHelper;
import io.nerv.web.sys.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWT鉴权
 * @author: S.PKAQ
 */
@Slf4j
@Tag(name = "用户信息")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthCtrl {
    private final UserService userService;
    private final DictCacheHelper dictCacheHelper;

    @GetMapping("/fetchMenus")
    @Operation(summary = "获取当前登录用户的信息(菜单.权限.消息)")
    public Response fetchMenus()  {

        try {
            final var userId = ThreadUserHelper.getUserId();

            return new Response().success(this.userService.fetch(userId));
        } catch (Exception e) {
            return new Response().failure(BizCodeEnum.SERVER_ERROR);
        }
    }

    @GetMapping("/fetchDicts")
    @Operation(summary = "获取字典信息")
    public Response fetchDicts()  {
        return new Response().success(dictCacheHelper.getAll());
    }
}
