package org.pkaq.pay.alipay.common;

import com.alipay.api.AlipayApiException;
import org.pkaq.pay.alipay.util.AlipayHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 默认访问返回
 * @author: S.PKAQ
 * @Datetime: 2018/9/29 11:01
 */
@RestController
public class IndexCtrl {
    @Autowired
    private AlipayHelper alipayHelper;

    @RequestMapping("/")
    public String Index(){
        return "Hello AliPay";
    }

    @RequestMapping("pre")
    public String prePay(){
        try {
            return alipayHelper.prepay("1234567","33","hahaha","3000");
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }
}
