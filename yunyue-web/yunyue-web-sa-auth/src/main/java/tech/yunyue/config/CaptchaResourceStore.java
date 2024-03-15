package tech.yunyue.config;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.generator.common.constant.SliderCaptchaConstant;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.common.model.dto.ResourceMap;
import cloud.tianai.captcha.resource.impl.DefaultResourceStore;
import cloud.tianai.captcha.resource.impl.provider.ClassPathResourceProvider;
import org.springframework.stereotype.Component;
import tech.yunyue.core.properties.EvaConfig;

import java.util.List;
import java.util.Optional;

import static cloud.tianai.captcha.generator.impl.StandardSliderImageCaptchaGenerator.DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH;

/**
 * @Description 负责验证码模板和背景图存储的地方
 */
@Component
public class CaptchaResourceStore extends DefaultResourceStore {
    private static final String DEFAULT_TAG = "default";
    private static final List<String> DEFAULT_BG_IMG = List.of("bgimages/picture.jpg", "bgimages/picture2.jpg");

    public CaptchaResourceStore(EvaConfig evaConfig) {
        // 滑块验证码 模板 (系统内置) @Autowired
        ResourceMap template1 = new ResourceMap(DEFAULT_TAG, 4);
        template1.put(SliderCaptchaConstant.TEMPLATE_ACTIVE_IMAGE_NAME, new Resource(ClassPathResourceProvider.NAME, DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/1/active.png")));
        template1.put(SliderCaptchaConstant.TEMPLATE_FIXED_IMAGE_NAME, new Resource(ClassPathResourceProvider.NAME, DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/1/fixed.png")));
        ResourceMap template2 = new ResourceMap(DEFAULT_TAG, 4);
        template2.put(SliderCaptchaConstant.TEMPLATE_ACTIVE_IMAGE_NAME, new Resource(ClassPathResourceProvider.NAME, DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/2/active.png")));
        template2.put(SliderCaptchaConstant.TEMPLATE_FIXED_IMAGE_NAME, new Resource(ClassPathResourceProvider.NAME, DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/2/fixed.png")));

        // 1. 添加一些模板
        addTemplate(CaptchaTypeConstant.SLIDER, template1);
        addTemplate(CaptchaTypeConstant.SLIDER, template2);

        // 2. 添加自定义背景图片 优先选用配置文件中的背景图
        Optional.ofNullable(evaConfig.getLoginBGImageURLs()).orElse(DEFAULT_BG_IMG)
                .forEach(img -> addResource(CaptchaTypeConstant.SLIDER, new Resource(ClassPathResourceProvider.NAME, img, DEFAULT_TAG)));
    }
}
