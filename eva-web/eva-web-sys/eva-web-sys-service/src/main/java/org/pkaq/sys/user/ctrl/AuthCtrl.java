package org.pkaq.sys.user.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.sys.dict.service.DictService;
import org.pkaq.sys.module.service.ModuleService;
import org.pkaq.sys.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author PKAQ
 */
@Slf4j
@Tag(name = "用户信息")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthCtrl extends Ctrl {
    private final DictService dictService;
    private final ModuleService moduleService;
    private final UserService userService;

    @GetMapping("/fetchMenus")
    @Operation(summary = "获取当前登录用户的信息(菜单.权限.消息)")
    public Response<Object> fetchMenus() {
        try {
            final var userId = ThreadUserHelper.getUserId();

            return success(this.moduleService.fetchUserModules(userId));
        } catch (Exception e) {
            return failure(CommonCodes.SERVER_ERROR);
        }
    }

    @GetMapping("/fetchDicts")
    @Operation(summary = "获取字典信息")
    public Response<Object> fetchDicts() {
        return success(dictService.fetchDicts());
    }
}
