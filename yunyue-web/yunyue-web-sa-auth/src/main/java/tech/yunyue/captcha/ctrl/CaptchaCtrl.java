/*
 *Copyright © 2018 anji-plus
 *安吉加加信息技术有限公司
 *http://www.anji-plus.com
 *All rights reserved.
 */
package tech.yunyue.captcha.ctrl;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.spring.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.vo.CaptchaResponse;
import cloud.tianai.captcha.spring.vo.ImageCaptchaVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.yunyue.captcha.bo.CheckCaptchaBo;

import java.util.Collections;

@RestController
@RequestMapping("/captcha")
@Tag(name = "验证码")
@Slf4j
public class CaptchaCtrl {
    @Autowired
    private ImageCaptchaApplication application;

    @PostMapping("/get")
    public CaptchaResponse<ImageCaptchaVO> get() {
        return application.generateCaptcha(CaptchaTypeConstant.SLIDER);
    }

    @PostMapping("/check")
    @ResponseBody
    public ApiResponse<?> checkCaptcha(@RequestBody @Validated CheckCaptchaBo bo) {
        ApiResponse<?> response = application.matching(bo.getId(), bo.getData());
        if (response.isSuccess()) {
            return ApiResponse.ofSuccess(Collections.singletonMap("id", bo.getId()));
        }
        return response;
    }
}
