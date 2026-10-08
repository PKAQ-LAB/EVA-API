package org.pkaq.core.auth.spi;

import java.util.List;

/**
 * 资源权限读取端口，屏蔽缓存和数据库实现。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public interface IResourcePermissionQuery {
    /**
     * 查询角色是否允许访问资源。
     * @param tenantId 可信租户编号
     * @param roleIds 可信角色编号
     * @param method 请求方法
     * @param path 请求路径
     * @return 是否允许访问
     */
    boolean hasPermission(Long tenantId, List<Long> roleIds, String method, String path);
}
