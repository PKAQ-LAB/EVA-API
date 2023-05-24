package tech.yunyue.sys.user.ctrl;

import tech.yunyue.sys.dict.service.DictService;
import tech.yunyue.sys.module.service.ModuleService;
import tech.yunyue.sys.user.service.UserService;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.threaduser.ThreadUserHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWT鉴权
 *
 * @author: S.PKAQ
 */
@Slf4j
@Tag(name = "用户信息")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthCtrl {
    private final UserService userService;
    private final DictService dictService;
    private final ModuleService moduleService;

    @GetMapping("/fetchMenus")
    @Operation(summary = "获取当前登录用户的信息(菜单.权限.消息)")
    public Response fetchMenus() {

        try {
            final var userId = ThreadUserHelper.getUserId();

            return new Response().success(this.userService.fetch(userId));
        } catch (Exception e) {
            return new Response().failure(BizCodeEnum.SERVER_ERROR);
        }
    }

    @GetMapping("/init")
    @Operation(summary = "返回登录用户的基本信息/资源信息/参数配置/列头配置")
    public Response init() {
        return new Response().success(this.userService.init());
    }

    @GetMapping("/fetchDicts")
    @Operation(summary = "获取字典信息")
    public Response fetchDicts() {
        return new Response().success(dictService.fetchDicts());
    }

    @GetMapping("/fetchResourcese")
    @Operation(summary = "系统全部可用模块的资源列表")
    public Response fetchAllResourcese() {
        return new Response().success(moduleService.fetchResourcese());
    }
}
