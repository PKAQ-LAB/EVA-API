package org.pkaq.core.auth.spi;

import java.util.List;
import java.util.Map;

/**
 * 旧版动态 URL 决策使用的角色资源读取端口。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public interface IRoleResourceQuery {
    /**
     * 查询角色编码与资源路径映射。
     * @return 角色资源列表
     */
    List<Map<String, String>> listRoleNamesWithPath();
}
