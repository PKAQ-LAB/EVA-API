package org.pkaq.core.auth.security.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 登录成功响应
 *
 * @author PKAQ
 */
@Data
public class LoginSuccessVo {
    @JsonProperty("user_info")
    private LoginUserInfoVo userInfo;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;
}
