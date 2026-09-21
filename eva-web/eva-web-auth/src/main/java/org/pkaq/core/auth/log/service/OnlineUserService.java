package org.pkaq.core.auth.log.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.log.vo.OnlineUserVo;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 在线用户查询与下线服务。
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class OnlineUserService {
    private final CacheTokenUtil tokenUtil;
    private final EvaConfig evaConfig;

    /**
     * 查询当前可管理租户范围内的在线用户。
     *
     * @param userId 可选用户ID
     * @param targetTenantId 平台管理员可选目标租户ID
     * @return 在线用户列表
     */
    public List<OnlineUserVo> list(Long userId, Long targetTenantId) {
        Long tenantId = resolveTenantId(targetTenantId);
        Map<?, ?> tokens = tokenUtil.getTokens(tenantId);
        if (tokens == null || tokens.isEmpty()) {
            return Collections.emptyList();
        }
        return tokens.entrySet().stream()
                .filter(entry -> tenantId == null || String.valueOf(entry.getKey()).startsWith(tenantId + ":"))
                .map(entry -> toVo(entry.getKey(), entry.getValue()))
                .filter(vo -> vo != null && (userId == null || userId.equals(vo.getUserId())))
                .toList();
    }

    /**
     * 将指定用户强制下线。
     *
     * @param userId 用户ID
     * @param targetTenantId 平台管理员可选目标租户ID
     */
    public void offline(Long userId, Long targetTenantId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        Long tenantId = resolveTenantId(targetTenantId);
        if (tenantId == null) {
            throw new IllegalArgumentException("平台模式下必须指定目标租户ID");
        }
        tokenUtil.removeToken(tenantId, userId);
    }

    private Long resolveTenantId(Long targetTenantId) {
        if (evaConfig.isStandaloneMode()) {
            return 0L;
        }
        ThreadUser currentUser = ThreadUserHelper.getCurrentUserOrNull();
        if (currentUser == null) {
            throw new SecurityException("未登录用户不能管理在线会话");
        }
        if (evaConfig.isPlatformMode() && ThreadUserHelper.isAdmin()) {
            if (targetTenantId != null && targetTenantId < 0L) {
                throw new IllegalArgumentException("目标租户ID不能为负数");
            }
            return targetTenantId;
        }
        return currentUser.getTenantId();
    }

    private OnlineUserVo toVo(Object cacheKey, Object value) {
        if (!(value instanceof Map<?, ?> token)) {
            return null;
        }
        String[] keyParts = String.valueOf(cacheKey).split(":", 2);
        if (keyParts.length != 2) {
            return null;
        }
        try {
            OnlineUserVo vo = new OnlineUserVo();
            vo.setTenantId(Long.valueOf(keyParts[0]));
            vo.setUserId(Long.valueOf(keyParts[1]));
            vo.setDevice(stringValue(token.get("device")));
            vo.setVersion(stringValue(token.get("version")));
            vo.setIssuedAt(token.get("issuedAt"));
            vo.setExpireAt(token.get("expireAt"));
            vo.setLoginTime(token.get("loginTime"));
            return vo;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
