package tech.yunyue.captcha.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "验证码验证参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class CheckCaptchaBo {
    /**
     * id
     */
    @Schema(description = "id")
    @NotEmpty(message = "验证码ID不能为空")
    private String id;

    /**
     * data
     */
    @Schema(description = "data")
    @NotNull(message = "滑动轨迹不能为空")
    private DateImageCaptchaTrack data;
}
