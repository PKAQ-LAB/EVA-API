package tech.yunyue.config;


import cloud.tianai.captcha.generator.ImageCaptchaGenerator;
import cloud.tianai.captcha.spring.application.DefaultImageCaptchaApplication;
import cloud.tianai.captcha.spring.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.autoconfiguration.ImageCaptchaProperties;
import cloud.tianai.captcha.spring.store.CacheStore;
import cloud.tianai.captcha.validator.ImageCaptchaValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

/**
 * @Description 使用自定义的强制二次验证替换原有二次验证自动装配
 */
@Order
@Configuration
public class CaptchaAutoConfiguration {

    /**
     * 使用自定义的强制二次验证替换原有二次验证
     *
     * @param captchaGenerator
     * @param imageCaptchaValidator
     * @param cacheStore
     * @param prop
     * @return
     */
    @Bean
    @Primary
    public ImageCaptchaApplication forceLoginSecondaryVerificationApplication(ImageCaptchaGenerator captchaGenerator,
                                                                              ImageCaptchaValidator imageCaptchaValidator,
                                                                              CacheStore cacheStore,
                                                                              ImageCaptchaProperties prop) {
        ImageCaptchaApplication target = new DefaultImageCaptchaApplication(captchaGenerator, imageCaptchaValidator, cacheStore, prop);
        if (prop.getSecondary() != null && Boolean.TRUE.equals(prop.getSecondary().getEnabled())) {
            target = new ForceLoginSecondaryVerificationApplication(target, prop.getSecondary());
        }
        return target;
    }
}
