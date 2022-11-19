package io.nerv.core.weixin.ctrl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.XmlUtil;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.util.SignUtils;
import io.nerv.config.WxpayConfig;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.weixin.util.IpUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Tag(name = "微信支付")
@Slf4j
@RestController
@RequestMapping("/wx/pay")
public class WxPayController {
  private final WxPayService payService;

  private final WxpayConfig payConfig;

  private final WxMpService wxMpService;

  public WxPayController(WxPayService payService, WxpayConfig payConfig, WxMpService wxMpService) {
    this.payService = payService;
    this.payConfig = payConfig;
    this.wxMpService = wxMpService;
  }

  /**
   * 返回前台H5调用JS支付所需要的参数，公众号支付调用此接口
   */
  @RequestMapping(value = "getJSSDKPayInfo")
  public Response getJSSDKPayInfo(HttpServletRequest request, String code) throws WxErrorException {

    Map<String, String> payInfo = null;

    // 获取openid
    WxOAuth2AccessToken wxMpOAuth2AccessToken = this.wxMpService.getOAuth2Service().getAccessToken(code);
    WxOAuth2UserInfo wxMpUser = this.wxMpService.getOAuth2Service().getUserInfo(wxMpOAuth2AccessToken, null);

    String body = "IPHONE";

    WxPayUnifiedOrderRequest prepayInfo = WxPayUnifiedOrderRequest.newBuilder()
            .openid(wxMpUser.getOpenid())
            .outTradeNo(IdUtil.getSnowflake(1, 1).nextIdStr())
            .totalFee(1)
            .body(body)
            .tradeType(WxPayConstants.TradeType.JSAPI)
            .spbillCreateIp(IpUtil.getIPAddress(request))
            .notifyUrl("http://paytest.relaxgroup.cn/wx/pay/getJSSDKCallbackData")
            .build();
//    prepayInfo.setSignType(WxPayConstants.SignType.MD5);

    try {
      payInfo = this.payService.createOrder(prepayInfo);
    } catch (WxPayException e) {
      e.printStackTrace();
    }

    return new Response().success(payInfo);
  }

  /**
   * 微信通知支付结果的回调地址，notify_url
   */
  @RequestMapping(value = "getJSSDKCallbackData")
  public Response getJSSDKCallbackData(HttpServletRequest request) {
    Response response = null;
    try {
      synchronized (this) {
        Document document =  XmlUtil.readXML(request.getInputStream());

        Map<String, Object> kvm = XmlUtil.xmlToMap(document.toString());
        if (SignUtils.checkSign(kvm, null, this.payConfig.getMchKey())) {
          if (kvm.get("result_code").equals("SUCCESS")) {
            log.info("out_trade_no: " + kvm.get("out_trade_no") + " pay SUCCESS!");
            response = new Response().success("支付成功");
          } else {
            this.log.error("out_trade_no: "
                    + kvm.get("out_trade_no") + " result_code is FAIL");
            response = new Response().success("支付失败");
          }
        } else {
          response = new Response().failure(BizCodeEnum.FILETYPE_NOT_SUPPORTED);
          this.log.error("out_trade_no: " + kvm.get("out_trade_no")
                  + " check signature FAIL");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return response;
  }
}