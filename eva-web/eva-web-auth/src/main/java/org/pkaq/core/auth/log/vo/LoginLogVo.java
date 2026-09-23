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

    @Schema(description = "登录会话ID")
    private String sessionId;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "设备型号，浏览器无法识别时为UNKNOWN")
    private String deviceModel;

    @Schema(description = "操作系统")
    private String osName;

    @Schema(description = "操作系统版本")
    private String osVersion;

    @Schema(description = "浏览器")
    private String browserName;

    @Schema(description = "浏览器版本")
    private String browserVersion;

    @Schema(description = "设备特征摘要")
    private String deviceFingerprint;

    @Schema(description = "风险标记")
    private String riskFlags;

    @Schema(description = "最后活动时间")
    private LocalDateTime lastActiveAt;

    @Schema(description = "退出时间")
    private LocalDateTime logoutAt;

    @Schema(description = "退出原因")
    private String logoutReason;

    @Schema(description = "创建时间")
    private LocalDateTime utcCreate;
}
