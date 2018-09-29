package org.pkaq.pay.alipay.response;

import cn.hutool.core.util.StrUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝回调接口
 * @author: S.PKAQ
 * @Datetime: 2018/9/29 10:30
 */
@Slf4j
@RestController
@RequestMapping(value = "alipay")
public class AlipayResponseCtrl {

    private final static String APP_ID = "2018092561531306";
    private final static String CHARSET = "UTF-8";
    private final static String SIGN_TYPE = "RSA2";
    private final static String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAghqnxLzdt+sZmjbXJ19mYmvOjckNwA5Nwrvc+XcgZzuHdT+HmERiLS1OC/f5ioLkzg31+IB6ECX1ErJANQXAOLBbmUm8A/SK+dchf4Qm8EjP+e6cygi+p6bDG5BtV1k+PQYivm0uVJgosErD9qj6bptwYiFQ2Pe70z4groZxvUJEfKFngQ1/oarkaAt5KtlLlyfEPnttHb39QA3t9htdzLrsi0qIq3uecjpFIEBRHHZjm81fHbuBZN37oaQ7CxlKbohZMzxtdFYswKY4E1oLWZ1L2jvDtUEOa4PrsmLim8EI1MZUNoB0aRrVLUDRJsqNdHkNNDh2jZoOzlsFBMCh1wIDAQAB";

    @PostMapping("callback")
    public void alipayCallback(HttpServletRequest request, HttpServletResponse response){
        String  message = "success";
        Map<String, String> params = new HashMap<>();
        // 取出所有参数是为了验证签名
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            if ("sign_type".equals(parameterName) || StrUtil.isBlank(request.getParameter(parameterName))) {
                continue;
            }
            params.put(parameterName, request.getParameter(parameterName));
        }
        //验证签名 校验签名
        boolean signVerified = false;
        try {
            signVerified = AlipaySignature.rsaCheckV2(params, ALIPAY_PUBLIC_KEY, CHARSET, SIGN_TYPE);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            message =  "failed";
        }
        if (signVerified) {
            log.info("支付宝验证签名成功！");
            // 若参数中的appid和填入的appid不相同，则为异常通知
            if (!APP_ID.equals(params.get("app_id"))) {
                log.info("与付款时的appid不同，此为异常通知，应忽略！");
                message =  "failed";
            }else{
                String outtradeno = params.get("out_trade_no");
                //在数据库中查找订单号对应的订单，并将其金额与数据库中的金额对比，若对不上，也为异常通知
                String status = params.get("trade_status");
                if ("WAIT_BUYER_PAY".equals(status)) {
                    // 如果状态是正在等待用户付款
                    log.info(outtradeno + "订单的状态正在等待用户付款");
                } else if ("TRADE_CLOSED".equals(status)) {
                    // 如果状态是未付款交易超时关闭，或支付完成后全额退款
                    log.info(outtradeno + "订单的状态已经关闭");
                } else if ("TRADE_SUCCESS".equals(status) || "TRADE_FINISHED".equals(status)) {
                    // 如果状态是已经支付成功
                    log.info("(支付宝订单号:"+outtradeno+"付款成功)");
                    //这里 根据实际业务场景 做相应的操作
                }
            }
        } else {
            // 如果验证签名没有通过
            message =  "failed";
            log.info("验证签名失败！");
        }

        try(BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream())){
            out.write(message.getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
