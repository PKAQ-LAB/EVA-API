package io.nerv.weixin.ctrl;

import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.util.Response;
import io.nerv.weixin.exception.WeixinException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

@Slf4j
@RestController
@AllArgsConstructor
@Api("需要鉴权才能调用的微信相关接口")
@RequestMapping("/wx/auth")
public class WxAuthController {

    private final WxMpService wxMpService;

    @PostMapping("/getJsapiTicket")
    @ApiOperation(value = "获取jsapi_ticket",response = Response.class)
    public Response getJsapiTicket() throws WeixinException {
        String ticket = null;
        try {
            ticket = this.wxMpService.getJsapiTicket();
        } catch (WxErrorException e) {
            throw new WeixinException(BizCodeEnum.WEIXIN_JSTICKET_ERROR);
        }

        return new Response().success(ticket);
    }

    @PostMapping(value = "/getShareSign")
    @ApiOperation(value = "获取分享签名",response = Response.class)
    public Response getShareSign(String url) throws WxErrorException {
        url = URLDecoder.decode(url, Charset.defaultCharset());

        WxJsapiSignature sign = wxMpService.createJsapiSignature(url);
        return new Response().success(sign);
    }

    @GetMapping("/getUserInfo")
    @ApiOperation(value = "获取用户微信账号",response = Response.class)
    public Response getUserInfo(@ApiParam(name ="code", value = "用户code")
                                        String code,
                                @ApiParam(name ="appid", value = "应用Id")
                                @PathVariable String appId) {

        if (!this.wxMpService.switchover(appId)) {
            throw new IllegalArgumentException(String.format("未找到对应appId=[%s]的配置，请核实！", appId));
        }

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