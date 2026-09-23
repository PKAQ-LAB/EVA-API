package org.pkaq.core.auth.log.vo;

import lombok.Data;
import org.pkaq.core.mvc.vo.Vo;

/**
 * 在线用户视图；禁止向前端返回原始 Token。
 *
 * @author PKAQ
 */
@Data
public class OnlineUserVo implements Vo {
    private Long tenantId;
    private Long userId;
    private String sessionId;
    private String device;
    private String version;
    private String ip;
    private String deviceModel;
    private String osName;
    private String osVersion;
    private String browserName;
    private String browserVersion;
    private Object issuedAt;
    private Object expireAt;
    private Object loginTime;
    private Object lastActiveAt;
}
