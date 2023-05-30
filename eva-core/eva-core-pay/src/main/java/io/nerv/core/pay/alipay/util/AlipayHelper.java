package io.nerv.core.pay.alipay.util;

import cn.hutool.json.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import io.nerv.config.AlipayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 支付宝工具类
 *
 * @author: S.PKAQ
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlipayHelper {
    private final AlipayConfig alipayConfig;

    private final AlipayClient alipayClient;

    /**
     * alipay.trade.query(统一收单线下交易查询)
     *
     * @param out_trade_no
     * @return
     * @throws AlipayApiException
     */
    public AlipayTradeQueryResponse payquery(String out_trade_no) throws AlipayApiException {
        AlipayTradeQueryRequest queryRequest = new AlipayTradeQueryRequest();
        JSONObject json = new JSONObject();
        json.set("out_trade_no", out_trade_no);

        queryRequest.setBizContent(json.toString());

        return alipayClient.execute(queryRequest);
    }

    /**
     * 交易预创建（二维码生成）
     *
     * @param orderNo     订单号 16位唯一
     * @param totalAmount 订单价格
     * @param subject     标题
     * @param timeout     付款时间
     * @throws AlipayApiException
     */
    public String prepay(String orderNo, String totalAmount, String subject, String timeout) throws AlipayApiException {
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();

        JSONObject json = new JSONObject();
        json.set("out_trade_no", orderNo);
        json.set("total_amount", totalAmount);
        json.set("subject", subject);
        json.set("timeout_express", timeout);

        log.debug(json.toString());

        request.setNotifyUrl(alipayConfig.getNotify_url());

        request.setBizContent(json.toString());

        AlipayTradePrecreateResponse response = alipayClient.execute(request);

        if (response.isSuccess()) {
            log.debug("调用成功");
            return response.getQrCode();
        } else {
            log.debug("调用失败");
            return null;
        }
    }
}
