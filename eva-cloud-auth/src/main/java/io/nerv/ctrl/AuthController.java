package io.nerv.ctrl;

import io.nerv.core.enums.BizCode;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.OathException;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.security.domain.JwtUserDetail;
import io.nerv.core.util.SecurityHelper;
import io.nerv.domain.AuthEntity;
import io.nerv.web.sys.user.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;

/**
 * 自定义Oauth2获取令牌接口
 */
@Slf4j
@RestController
@RequestMapping("/oauth")
public class AuthController {

    @Autowired
    private TokenEndpoint tokenEndpoint;

    @Autowired
    private SecurityHelper securityHelper;

    @Autowired
    private UserService userService;

    /**
     * Oauth2登录认证
     */
    @PostMapping(value = "/token")
    public Response postAccessToken(Principal principal, @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {

        log.info("-------------  token  -------------");

        OAuth2AccessToken oAuth2AccessToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();

        AuthEntity oauth2TokenDto = AuthEntity.builder()
                .tk_alpha(oAuth2AccessToken.getValue())
                .tk_bravo(oAuth2AccessToken.getRefreshToken().getValue())
                .build();

        return new Response().success(oauth2TokenDto);
    }

    /**
     * 登出
     * @param request
     * @return
     */
    @RequestMapping(value = "/logout")
    public Response logOut(HttpServletRequest request){
        new SecurityContextLogoutHandler().logout(request, null, null);
        return new Response().success(null, BizCodeEnum.LOGINOUT_SUCCESS);
    }

    @GetMapping("/fetch")
    @ApiOperation(value = "获取当前登录用户的信息(菜单.权限.消息)",response = Response.class)
    public Response fetch() throws OathException {
        final var id = securityHelper.getJwtUserId();

        return new Response().success(this.userService.fetch(id));
    }
}