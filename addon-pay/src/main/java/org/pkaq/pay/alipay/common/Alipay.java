package org.pkaq.pay.alipay.common;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.json.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.internal.util.AlipayUtils;
import com.alipay.api.internal.util.codec.Base64;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.springframework.util.Base64Utils;

import java.io.File;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/9/25 9:48
 */
public class Alipay {
    private Alipay(){}
    /** 支付宝网关 **/
    private final static String URL = "https://openapi.alipay.com/gateway.do";
    /** APPID即创建应用后生成 **/
    private final static String APP_ID = "2018092561531306";
    /** 开发者应用私钥，由开发者自己生成 **/
    private final static String APP_PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDQe4Z5mBj5Iz0o" +
            "rzaFckRVuXgNSzOjr7QD7yNiHghmuLTx/shP5OXipsjmC1n74m/aeC0LZj6FznKm" +
            "AHt0Wvh6LKQW+/OtDMkRMCvfT86UuEYjoHO028negXOQ6ezXQq/tpt9uXvXqu7XO" +
            "d1K5FHHVvS1tn2Ar/FYQ/LRexM95n//lCb9urmCOhSAdZSXZ/bYrm92GqncsG7MP" +
            "iwRmvZU+rxoNdvEvlRkQj9gfjdLGv36ZG4Upsyh3xxHbe5XyG9LXV02wEXVJM7WL" +
            "gYwb8ge9RCb4PFXcXFcytS3J8CdH2GZBSO8qKxHIeBernonB9pVf1l8uf8hJ4A0O" +
            "TXh+2PjjAgMBAAECggEAEkgyw6jtz7MiYtXyjwM1MNtBxtCoZ5s5yvqGfM0raW6b" +
            "F3mDMVZePmDnzxyBuI/jxSGyuYQuyWcQMLRsHwj3LyeJACb5+Fvdqe8yebOy5uG9" +
            "7NPeMabwbd2mN7f7t+i2Kr3ua+xR3lK1n7RhAa/kR5z+RdWrBlBrb7PpUUIq+3w+" +
            "qenEuPIhkaWJAaSwrTUJvcKP5tlD/xizp5QuehEmfaN922zHv0SrLaaamXZwxgRk" +
            "2Mcss9d48EfSiRlFkg+cd7rLgaQshuO/rQCQ0ghmQX6Ymna0GNtkD7pdd0gkMAp9" +
            "AyH4piPOm6fZArPE1apDcy7ldDhkPEYyNGqyrm2IsQKBgQD/E0SMvbedI6HG0cSb" +
            "fKB3qPnPqEjjhVvh71HBu7wcuGhe8n41Y6J4Ny40JOi9iZaIU6o4j5V30ttK9Ul8" +
            "SVByNcP2SC/hM7lvGeg69tr09BWt2Pmx2EnGAtWZoDFaYSqExXI1SU+bRXxX6t0k" +
            "zdHH36rD7iX8iYYpsl7reCC5KQKBgQDRPQPu/uEuPw0aWGzPPF5+ZZOAxsPOKtat" +
            "96aG2vPnftaLZsI/VflhF08CdL2pSTlnuDvptiCBkjkGsO2xocrz+IQB54+ygVpx" +
            "4t2zkAhnvOTKl5twOsvsJH7RhGluab63Z9a8Xi/A1ixRY/pjVKsZyUw6wwIRCKDf" +
            "GZvTlHPHKwKBgFNHYiklQ86NNKNqF2vAe8+1nqIaynlmMAKLFUFQXvFtj734JBag" +
            "Sgo/0dj6d/Zx9nDaCAhhcKZndGt7vBtVAGvZe/prxR4cuh048yZVNFWnX3CcF6Hm" +
            "AOgiQsTelV5xvOUOZeRZ5CDIxfkaWEtM24ILV4CSv1o9OE1izDTKyrxJAoGAQSze" +
            "Us212q/tJu2wRwUo8fo17gD78PZLFLl1d8AwBUXvElEZ5JylJ2cZR0yR329doD80" +
            "+mGu0m13sevevlwFWeVpSSaWDiZYw+zE3i/ZTmb6ZVjAJD9MUew0TJMQPEJYE80W" +
            "iGpWOZz9BQU9cz9mZIvdYNhphmDHq3bQoSE2xy0CgYAxX2Ki+eojBuW770/9w3OI" +
            "Lb0SXGPv5gq9I5Y8IslRDWNYKIwC52Cua7Irw+TnZ3k+JiOZijyfLA9L9PTlWZYH" +
            "3MCJmMxyqvHg0bFRLIFgE7ONpGwG19nHrFJrGhITVVHZobR0jZBQyKp0xwOBhJQT" +
            "RvGE7Y4SUObc2aIMnDUlAQ==";
    /** 参数返回格式，只支持json **/
    private final static String FORMAT = "json";
    /** 请求和签名使用的字符编码格式，支持GBK和UTF-8 **/
    private final static String CHARSET = "UTF-8";
    /** 支付宝公钥，由支付宝生成 **/
    private final static String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAghqnxLzdt+sZmjbXJ19mYmvOjckNwA5Nwrvc+XcgZzuHdT+HmERiLS1OC/f5ioLkzg31+IB6ECX1ErJANQXAOLBbmUm8A/SK+dchf4Qm8EjP+e6cygi+p6bDG5BtV1k+PQYivm0uVJgosErD9qj6bptwYiFQ2Pe70z4groZxvUJEfKFngQ1/oarkaAt5KtlLlyfEPnttHb39QA3t9htdzLrsi0qIq3uecjpFIEBRHHZjm81fHbuBZN37oaQ7CxlKbohZMzxtdFYswKY4E1oLWZ1L2jvDtUEOa4PrsmLim8EI1MZUNoB0aRrVLUDRJsqNdHkNNDh2jZoOzlsFBMCh1wIDAQAB";
    /** 商户生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2 **/
    private final static String SIGN_TYPE = "RSA2";

    public static void main(String[] args) throws AlipayApiException {
        new Alipay().prepay();
        //new Alipay().payquery();
        //new Alipay().signCheck();
    }

    private static class AlipayHolder{
        private  static AlipayClient alipayClient = new DefaultAlipayClient(URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);
    }

    public static AlipayClient getAlipayClient(){
        return AlipayHolder.alipayClient;
    }

    public void signCheck() throws AlipayApiException {
        String sign = "QMbHXRUae2dOIaCVWvHsLwRLPgjWCHPh7Fu9n0uZFePLjL4+6T3M4MGhXISm3e5CMz312I9xnt1s/wh28Pw7DUn8Gr9uDoWaluwqMuthnMIoonkHDswC7ldeiU/mQa5ruIGLoo+VHGTsqYl2HQvckb6VReGABXd6TSJJlwjy6lAC4AtoKWR81fy5YtbTDchyByMqjRXiGHN0R+WS6eqanpi3cXxyeQEVchVrV9ZK1nsCqmk3Pgs2WsySCAfduMIQwoEzjWAx4//50oTr54hgfNNo6T4LJZut/ir8pfGd3n3/chrvfbAuAxeOGmB8lIO/3fAThUsOSIeZ9ENJLYK4LQ==";

        String params = "app_id=2018092561531306&auth_app_id=2018092561531306&buyer_id=2088512686518743&buyer_logon_id=185****0989&buyer_pay_amount=0.01&charset=UTF-8&fund_bill_list=[{\"amount\":\"0.01\",\"fundChannel\":\"ALIPAYACCOUNT\"}]&gmt_create=2018-09-29 17:03:58&gmt_payment=2018-09-29 17:04:03&invoice_amount=0.01&notify_id=2018092900222170403018740540416563&notify_time=2018-09-29 17:18:22&notify_type=trade_status_sync&out_trade_no=20150320010101008&point_amount=0.00&receipt_amount=0.01&seller_email=leiyu@donghaiair.cn&seller_id=2088231249733846&sign_type=RSA2&subject=测机上升舱费用-异步回调&total_amount=0.01&trade_no=2018092922001418740532348646&trade_status=TRADE_SUCCESS&version=1.0";
    }
    public void payquery() throws AlipayApiException {
        AlipayClient alipayClient = Alipay.getAlipayClient();
        AlipayTradeQueryRequest queryRequest = new AlipayTradeQueryRequest();
        JSONObject json = new JSONObject();
        json.put("out_trade_no", "20150320010101020");

        queryRequest.setBizContent(json.toString());

        AlipayTradeQueryResponse queryResponse =  alipayClient.execute(queryRequest);
        String tradeStatus = queryResponse.getTradeStatus();
        System.out.println(queryResponse.getBody());
        System.out.println(tradeStatus);

    }
    /**
     * 二维码创建
     */
    public void prepay() throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);
        //通过alipayClient调用API，获得对应的response类
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();

        JSONObject json = new JSONObject();
        json.put("out_trade_no", "20150320010101020");
        json.put("total_amount", "0.01");
        json.put("subject", "TESTJSSC");
        json.put("timeout_express", "15m");

        request.setNotifyUrl("http://97f06dd5.ngrok.io/alipay/alipay/callback");

        System.out.println(json.toString());
        request.setBizContent(json.toString());

        AlipayTradePrecreateResponse response = alipayClient.execute(request);

        if(response.isSuccess()){
            QrCodeUtil.generate(response.getQrCode(),500,500,new File("D:/qrcode.jpg"));
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
    }
}
