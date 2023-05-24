package io.nerv.core.weixin.ctrl;

import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.Charset;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "需要鉴权才能调用的微信相关接口")
@RequestMapping("/wx/auth")
public class WxAuthController {

    private final WxMpService wxMpService;

    @PostMapping("/getJsapiTicket")
    @Operation(description = "获取jsapi_ticket")
    public Response getJsapiTicket() {
        String ticket = null;
        try {
            ticket = this.wxMpService.getJsapiTicket();
        } catch (WxErrorException e) {
            BizCodeEnum.WEIXIN_JSTICKET_ERROR.newException();
        }

        return new Response().success(ticket);
    }

    @PostMapping(value = "/getShareSign")
    @Operation(description = "获取分享签名")
    public Response getShareSign(String url) throws WxErrorException {
        url = URLDecoder.decode(url, Charset.defaultCharset());

        WxJsapiSignature sign = wxMpService.createJsapiSignature(url);
        return new Response().success(sign);
    }

    @GetMapping("/getUserInfo")
    @Operation(description = "获取用户微信账号")
    public Response getUserInfo(@Parameter(name = "code")
                                String code,
                                @Parameter(name = "appid")
                                @PathVariable String appId) {

        if (!this.wxMpService.switchover(appId)) {
            throw new IllegalArgumentException(String.format("未找到对应appId=[%s]的配置，请核实！", appId));
        }

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