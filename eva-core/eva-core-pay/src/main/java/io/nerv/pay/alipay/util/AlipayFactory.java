package io.nerv.pay.alipay.util;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import io.nerv.config.AlipayConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author PKAQ
 */
@Data
@Component
@RequiredArgsConstructor
public class AlipayFactory {

    private final AlipayConfig alipayConfig;

    @Bean
    public AlipayClient getAlipayClient(){
        return new DefaultAlipayClient(alipayConfig.getURL(),
                alipayConfig.getAPP_ID(),
                alipayConfig.getAPP_PRIVATE_KEY(),
                alipayConfig.getFORMAT(),
                alipayConfig.getCHARSET(),
                alipayConfig.getALIPAY_PUBLIC_KEY(),
                alipayConfig.getSIGN_TYPE());
    }
}
