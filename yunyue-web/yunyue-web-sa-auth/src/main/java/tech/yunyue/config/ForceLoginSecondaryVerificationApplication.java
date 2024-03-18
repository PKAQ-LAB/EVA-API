package tech.yunyue.config;

import cloud.tianai.captcha.spring.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.autoconfiguration.SecondaryVerificationProperties;
import cloud.tianai.captcha.spring.plugins.secondary.SecondaryVerificationApplication;

import java.util.Map;

/**
 * @Description 强制登录二次验证
 */
public class ForceLoginSecondaryVerificationApplication extends SecondaryVerificationApplication {
    private SecondaryVerificationProperties prop;

    public ForceLoginSecondaryVerificationApplication(ImageCaptchaApplication target, SecondaryVerificationProperties prop) {
        super(target, prop);
    }

    /**
     * 二次缓存验证 强制登录才删除缓存，否则不删除
     *
     * @param id id
     * @return boolean
     */
    public boolean secondaryVerification(String id, boolean forceLogin) {
        Map<String, Object> cache = null;
        if (forceLogin) {
            cache = target.getCacheStore().getAndRemoveCache(getKey(id));
        } else {
            cache = target.getCacheStore().getCache(getKey(id));
        }
        return cache != null;
    }
}
