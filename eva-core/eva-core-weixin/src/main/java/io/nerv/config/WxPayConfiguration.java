package io.nerv.config;

import cn.hutool.core.util.StrUtil;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author PKAQ
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnClass(WxPayService.class)
@EnableConfigurationProperties(WxpayConfig.class)
public class WxPayConfiguration {

  private final WxpayConfig properties;

  @Bean
  @ConditionalOnMissingBean
  public WxPayService wxService() {
    WxPayConfig payConfig = new WxPayConfig();
    payConfig.setAppId(StrUtil.trimToNull(this.properties.getAppId()));
    payConfig.setMchId(StrUtil.trimToNull(this.properties.getMchId()));
    payConfig.setMchKey(StrUtil.trimToNull(this.properties.getMchKey()));
    payConfig.setSubAppId(StrUtil.trimToNull(this.properties.getSubAppId()));
    payConfig.setSubMchId(StrUtil.trimToNull(this.properties.getSubMchId()));
    payConfig.setKeyPath(StrUtil.trimToNull(this.properties.getKeyPath()));

    // 可以指定是否使用沙箱环境
    payConfig.setUseSandboxEnv(false);

    WxPayService wxPayService = new WxPayServiceImpl();
    wxPayService.setConfig(payConfig);
    return wxPayService;
  }

}