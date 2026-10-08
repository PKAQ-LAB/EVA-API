package org.pkaq.core.auth.authentication.ctrl;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.authentication.bo.RegistrationBo;
import org.pkaq.core.auth.authentication.service.AccountRegistrationService;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 默认关闭的独立模式自助注册接口，不返回登录Token。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@RestController
@RequiredArgsConstructor
public class RegistrationCtrl extends Ctrl {
    private final AccountRegistrationService registrationService;

    /** 创建独立账号，返回字符串账号ID。 */
    @PostMapping(value = "/auth/register", consumes = "application/json")
    public Response<String> register(@RequestBody RegistrationBo request) {
        return Response.success(registrationService.register(request));
    }
}
