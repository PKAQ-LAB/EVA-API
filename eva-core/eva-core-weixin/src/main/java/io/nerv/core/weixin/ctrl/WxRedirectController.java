package io.nerv.core.weixin.ctrl;

import io.nerv.core.mvc.vo.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "微信接口配置信息回调接口")
@AllArgsConstructor
@RestController
@RequestMapping("/wx/redirect")
public class WxRedirectController {
    private final WxMpService wxMpService;

    @GetMapping("/getUserInfo")
    @Operation(description = "获取用户微信账号")
    public Response getUserInfo(@Parameter(name = "code", description = "用户code")
                                String code) {

        WxOAuth2UserInfo wxMpUser = null;
        try {
            WxOAuth2AccessToken wxMpOAuth2AccessToken = this.wxMpService.getOAuth2Service().getAccessToken(code);
            wxMpUser = this.wxMpService.getOAuth2Service().getUserInfo(wxMpOAuth2AccessToken, null);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        return new Response().success(wxMpUser);
    }

}