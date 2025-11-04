package org.pkaq.core.auth.log.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final CacheTokenUtil tokenUtil;

    @GetMapping("/list")
    @Operation(description = "获取在线用户列表")
    public Response list(@Parameter(name = "uid", description = "查询固定用户") String account) {
//        Response response = new Response();
//        if (StrUtils.isNotBlank(account)) {
//            Object obj = this.tokenUtil.getToken(account);
//
//            List list = new ArrayList(1);
//
//            if (null != obj) {
//                list.add(obj);
//            }
//
//            response.setData(list);
//        } else {
//            response.setData(this.tokenUtil.getAllToken().values());
//        }
        return success();
    }

    @GetMapping("/offline")
    @Operation(description = "踢掉一个用户")
    public Response offlineUser(@Parameter(name = "uid", description = "要踢掉的用户id") String uid) {
        this.tokenUtil.removeToken(uid);
        return new Response().success();
    }
}
