package org.pkaq.sys.module.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.core.annotation.Ignore;

import java.util.List;
import java.util.Set;

/**
 * 仅供当前登录菜单使用的授权元数据查询。
 * @author PKAQ
 * @date 2026-10-28
 */
@Mapper
public interface AuthorizedMenuMapper {
    /**
     * 在可信本地 schema 中读取当前用户有效角色的资源引用。
     * @param userId 服务端认证用户编号
     * @param standalone 是否为独立模式
     * @return 完整有效角色资源编号
     */
    @Ignore
    Set<Long> selectCurrentResourceIds(@Param("userId") long userId, @Param("standalone") boolean standalone);

    /**
     * 在受控元数据 schema 中读取授权资源交集，不加载同模块兄弟资源。
     * @param resourceIds 本次可信用户查询所得资源编号
     * @param tenantId 服务端认证租户编号
     * @param schemaMode 是否需与平台租户授权求交
     * @return 最小菜单投影
     */
    @Ignore
    List<AuthorizedMenuRow> selectMenuRows(@Param("resourceIds") Set<Long> resourceIds,
                                         @Param("tenantId") long tenantId,
                                         @Param("schemaMode") boolean schemaMode);
}
