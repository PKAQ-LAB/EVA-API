package org.pkaq.core.auth.log.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.vo.Vo;

import java.time.LocalDateTime;

/**
 * 登录日志视图对象
 *
 * @author PKAQ
 */
@Data
@Schema(description = "登录日志视图对象")
public class LoginLogVo implements Vo {

    @Schema(description = "登录日志ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "登录账号")
    private String account;

    @Schema(description = "登录类型")
    private String loginType;

    @Schema(description = "是否登录成功")
    private Boolean success;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "IP地址")
    private String ip;

    @Schema(description = "User-Agent")
    private String userAgent;

    @Schema(description = "设备")
    private String device;

    @Schema(description = "版本")
    private String version;

    @Schema(description = "创建时间")
    private LocalDateTime utcCreate;
}
