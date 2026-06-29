package org.pkaq.core.auth.log.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.log.bo.LoginLogQueryBo;
import org.pkaq.core.auth.log.service.LoginLogService;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录日志控制器
 *
 * @author PKAQ
 */
@RestController
@RequestMapping("/monitor/log/login")
@Tag(name = "登录日志")
@RequiredArgsConstructor
public class LoginLogCtrl extends Ctrl {

    private final LoginLogService loginLogService;

    /**
     * 根据id获取登录日志详情
     *
     * @param id 登录日志ID
     * @return 登录日志详情
     */
    @GetMapping({"/get/{id}"})
    @Operation(summary = "根据id获取登录日志详情")
    public Response<Object> query(@Parameter(name = "id", description = "登录日志id")
                                  @PathVariable(name = "id") Long id) {
        return success(this.loginLogService.get(id));
    }

    /**
     * 获取登录日志列表
     *
     * @param queryBo 查询参数
     * @return 登录日志分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取登录日志列表")
    public Response<Object> list(LoginLogQueryBo queryBo) {
        return success(this.loginLogService.list(queryBo));
    }
}
