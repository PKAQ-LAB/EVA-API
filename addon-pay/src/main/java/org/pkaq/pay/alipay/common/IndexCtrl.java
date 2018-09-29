package org.pkaq.pay.alipay.common;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 默认访问返回
 * @author: S.PKAQ
 * @Datetime: 2018/9/29 11:01
 */
@RestController
public class IndexCtrl {
    @RequestMapping("/")
    public String Index(){
        return "Hello AliPay";
    }
}
