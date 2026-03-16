package org.pkaq.core.auth.openapi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

import java.time.LocalDateTime;

/**
 * OpenAPI应用凭证实体
 *
 * @author PKAQ
 */
@Data
@Alias("openApiAppCredential")
@TableName("api_app_credential")
@EqualsAndHashCode(callSuper = true)
public class AppCredentialEntity extends StdEntity {

    /**
     * AppKey(唯一标识)
     */
    @TableField("app_key")
    private String appKey;

    /**
     * AppSecret(BCrypt加密存储)
     */
    @TableField("app_secret")
    private String appSecret;

    /**
     * 应用名称
     */
    @TableField("app_name")
    private String appName;

    /**
     * 状态 1-启用, 0-禁用
     */
    @TableField("status")
    private Integer status;

    /**
     * QPS限制
     */
    @TableField("rate_limit")
    private Integer rateLimit;

    /**
     * API权限列表(JSON格式)
     * 例如: "*" 表示全部权限
     * 例如: "/api/v1/user/*,/api/v1/data/*" 表示特定路径
     */
    @TableField("api_permissions")
    private String apiPermissions;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * IP白名单(逗号分隔)
     */
    @TableField("ip_whitelist")
    private String ipWhitelist;

    /**
     * 检查AppKey是否有效
     *
     * @return true-有效, false-无效
     */
    public boolean isValid() {
        if (status == null || status != 1) {
            return false;
        }

        if (expireTime != null && LocalDateTime.now().isAfter(expireTime)) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否有API访问权限
     *
     * @param apiPath API路径
     * @return true-有权限, false-无权限
     */
    public boolean hasApiPermission(String apiPath) {
        if (apiPermissions == null || apiPermissions.isEmpty()) {
            return true;
        }

        if ("*".equals(apiPermissions.trim())) {
            return true;
        }

        String[] permissions = apiPermissions.split(",");
        for (String permission : permissions) {
            String trimmed = permission.trim();

            if (trimmed.equals(apiPath)) {
                return true;
            }

            if (trimmed.endsWith("/*")) {
                String prefix = trimmed.substring(0, trimmed.length() - 2);
                if (apiPath.startsWith(prefix)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查IP是否在白名单内
     *
     * @param clientIp 客户端IP
     * @return true-在白名单, false-不在白名单
     */
    public boolean isIpAllowed(String clientIp) {
        if (ipWhitelist == null || ipWhitelist.isEmpty()) {
            return true;
        }

        String[] allowedIps = ipWhitelist.split(",");
        for (String ip : allowedIps) {
            if (ip.trim().equals(clientIp)) {
                return true;
            }
        }

        return false;
    }
}
