package io.nerv.weixin.ctrl;

import io.nerv.core.mvc.util.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

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

        WxMpUser wxMpUser = null;
        try {
            WxMpOAuth2AccessToken wxMpOAuth2AccessToken = this.wxMpService.oauth2getAccessToken(code);
            wxMpUser = this.wxMpService.oauth2getUserInfo(wxMpOAuth2AccessToken, null);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        return new Response().success(wxMpUser);
    }

}