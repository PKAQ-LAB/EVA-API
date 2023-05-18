package tech.yunyue.config;

import com.anji.captcha.config.AjCaptchaServiceAutoConfiguration;
import com.anji.captcha.config.AjCaptchaStorageAutoConfiguration;
import com.anji.captcha.properties.AjCaptchaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * aj_captcha的自动配置类  原自动配置类[AjCaptchaAutoConfiguration]会加载com.anji.captcha.controller.CaptchaController  <br>
 * CaptchaController使用了javax.servlet会报错<br>
 * 所以复制自动装配类 不扫描com.anji.captcha.controller.CaptchaController
 */
@Configuration
@EnableConfigurationProperties(AjCaptchaProperties.class)
@Import({AjCaptchaServiceAutoConfiguration.class, AjCaptchaStorageAutoConfiguration.class})
public class CAjCaptchaAutoConfiguration {
}
