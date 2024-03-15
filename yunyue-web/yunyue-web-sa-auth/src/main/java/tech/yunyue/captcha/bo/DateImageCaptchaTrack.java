package tech.yunyue.captcha.bo;

import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: mja
 * @Description 图片验证码滑动轨迹 给时间加上json序列化注解
 */
@Data
public class DateImageCaptchaTrack extends ImageCaptchaTrack {

    /**
     * 滑动开始时间.
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT+8")
    private Date startSlidingTime;
    /**
     * 滑动结束时间.
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT+8")
    private Date endSlidingTime;
}
