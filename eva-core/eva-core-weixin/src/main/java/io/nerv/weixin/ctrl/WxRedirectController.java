package io.nerv.weixin.ctrl;

import io.nerv.core.mvc.vo.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@Api("微信接口配置信息回调接口")
@AllArgsConstructor
@RestController
@RequestMapping("/wx/redirect")
public class WxRedirectController {
    private final WxMpService wxMpService;

    @GetMapping("/getUserInfo")
    @ApiOperation(value = "获取用户微信账号",response = Response.class)
    public Response getUserInfo(@ApiParam(name ="code", value = "用户code")
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