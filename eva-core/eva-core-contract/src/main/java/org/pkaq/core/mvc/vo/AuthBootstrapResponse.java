package org.pkaq.core.mvc.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * 仅用于登录和菜单初始化成功响应的当前资源权限能力快照。
 *
 * @param <T> 保持原结构的响应数据
 * @author PKAQ
 * @date 2026-10-28
 */
public final class AuthBootstrapResponse<T> extends Response<T> {
    @Getter
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private final boolean resourcePermissionEnabled;

    private AuthBootstrapResponse(boolean resourcePermissionEnabled) {
        this.resourcePermissionEnabled = resourcePermissionEnabled;
    }

    /**
     * 保留原成功响应的业务状态、国际化参数和数据，仅补充当前绑定能力。
     *
     * @param original 原成功响应
     * @param resourcePermissionEnabled 当前配置bean的资源权限开关
     * @param <T> 原响应数据类型
     * @return 携带能力快照的认证成功响应
     */
    public static <T> AuthBootstrapResponse<T> fromSuccess(Response<T> original, boolean resourcePermissionEnabled) {
        if (null == original || !original.isSuccess()) {
            throw new IllegalArgumentException("仅成功认证响应可提供资源权限能力");
        }
        AuthBootstrapResponse<T> response = new AuthBootstrapResponse<>(resourcePermissionEnabled);
        response.setSuccess(original.isSuccess());
        response.setCode(original.getCode());
        response.setMessage(original.getMessage());
        response.setMtype(original.getMtype());
        response.setArgs(original.getArgs());
        response.setData(original.getData());
        return response;
    }
}
