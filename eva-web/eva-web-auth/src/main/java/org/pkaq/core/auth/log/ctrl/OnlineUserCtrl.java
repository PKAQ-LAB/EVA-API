package org.pkaq.core.auth.log.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.log.service.OnlineUserService;
import org.pkaq.core.auth.log.vo.OnlineUserVo;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author PKAQ
 */
@RestController
@RequestMapping("/monitor/log/online")
@Tag(name = "获取在线用户")
@RequiredArgsConstructor
public class OnlineUserCtrl extends Ctrl {
    private final OnlineUserService onlineUserService;

    @GetMapping("/list")
    @Operation(description = "获取在线用户列表")
    public Response<java.util.List<OnlineUserVo>> list(
            @Parameter(name = "userId", description = "查询指定用户") Long userId,
            @Parameter(name = "targetTenantId", description = "平台管理员目标租户") Long targetTenantId) {
        return success(onlineUserService.list(userId, targetTenantId));
    }

    @PostMapping("/offline")
    @Operation(description = "踢掉一个用户")
    @BizLog(operateType = BizLogCodes.UPDATE, description = "强制用户下线[{0}]", args = {"param:0"})
    public Response<Object> offlineUser(@RequestParam Long userId,
                                        @RequestParam(required = false) Long targetTenantId) {
        onlineUserService.offline(userId, targetTenantId);
        return success();
    }
}
