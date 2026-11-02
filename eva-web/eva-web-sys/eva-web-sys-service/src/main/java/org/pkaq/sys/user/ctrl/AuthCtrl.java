package org.pkaq.sys.user.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.AuthBootstrapResponse;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.sys.dict.service.DictService;
import org.pkaq.sys.dict.service.DictSnapshot;
import org.pkaq.sys.dict.service.DictSourceException;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.module.service.AuthorizedMenuService;
import org.pkaq.sys.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
    private final AuthorizedMenuService authorizedMenuService;
    private final UserService userService;
    private final EvaConfig evaConfig;

    @GetMapping("/fetchMenus")
    @Operation(summary = "获取当前登录用户的信息(菜单.权限.消息)")
    public Response<Object> fetchMenus() {
        try {
            Response<Object> response = success(this.authorizedMenuService.fetchCurrentMenus());
            return AuthBootstrapResponse.fromSuccess(response, evaConfig.getResourcePermission().isEnable(),
                    evaConfig.getDataPermission().isEnable());
        } catch (Exception e) {
            log.error("获取当前用户菜单失败", e);
            return failure(CommonCodes.SERVER_ERROR);
        }
    }

    @GetMapping("/fetchDicts")
    @Operation(summary = "获取字典信息")
    public ResponseEntity<Response<Object>> fetchDicts() {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("private, no-store");
        headers.setVary(java.util.List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.COOKIE));
        try {
            DictSnapshot snapshot = dictService.snapshot();
            headers.setETag(snapshot.etag());
            // 正常200由Spring在统一响应Advice之前处理条件GET，命中时输出无体304。
            return new ResponseEntity<>(success(snapshot.data()), headers, HttpStatus.OK);
        } catch (DictSourceException exception) {
            return new ResponseEntity<>(failure(SysCodes.DICT_SNAPSHOT_INVALID), headers, HttpStatus.CONFLICT);
        }
    }
}
