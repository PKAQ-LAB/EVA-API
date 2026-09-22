package org.pkaq.core.auth.rbac.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.auth.rbac.entity.SysRoleResource;
import org.pkaq.core.auth.rbac.mapper.SysRoleResourceMapper;
import org.pkaq.core.auth.tenant.TenantAuthRoutingService;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 角色资源 Redis 缓存服务。
 *
 * @author PKAQ
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleResourceCacheService {

    private static final String CACHE_KEY_PREFIX = "eva:security:tenant:";
    private static final String ROLE_RESOURCE_SEGMENT = ":role-resource:";
    private static final String EMPTY_MARKER = "__EMPTY__";

    private final SysRoleResourceMapper roleResourceMapper;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final EvaConfig evaConfig;
    private final TenantAuthRoutingService tenantAuthRoutingService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * standalone 模式启动后预热角色资源；schema 模式按可信租户懒加载。
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional(rollbackFor = Exception.class)
    public void loadAllRoleResources() {
        if (!evaConfig.getResourcePermission().isEnable()) {
            log.info("资源权限未启用，跳过角色资源权限缓存加载");
            return;
        }
        if (evaConfig.getTenant().isSchemaMode()) {
            log.info("schema 租户模式按租户懒加载角色资源权限缓存");
            return;
        }
        List<Long> activeRoleIds = roleResourceMapper.selectActiveRoleIds();
        rebuildRoleResources(0L, activeRoleIds);
        log.info("角色资源权限缓存预热完成，共 {} 个角色", activeRoleIds.size());
    }

    /**
     * 刷新指定租户、指定角色的资源缓存。
     *
     * @param tenantId 租户ID
     * @param roleId 角色ID
     */
    public void refreshRoleCache(Long tenantId, Long roleId) {
        if (roleId == null) {
            return;
        }
        long trustedTenantId = trustedTenantId(tenantId);
        String key = cacheKey(trustedTenantId, roleId);
        List<SysRoleResource> resources = tenantAuthRoutingService.execute(
                trustedTenantId, () -> roleResourceMapper.selectByRoleId(roleId));
        redisTemplate.delete(key);
        Object[] values = resources.stream()
                .map(this::toCacheValue)
                .filter(value -> value != null)
                .toArray();
        if (values.length == 0) {
            redisTemplate.opsForSet().add(key, EMPTY_MARKER);
            return;
        }
        redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 重建指定租户、指定角色的扁平资源表并刷新缓存。
     *
     * @param tenantId 租户ID
     * @param roleId 角色ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void rebuildRoleResource(Long tenantId, Long roleId) {
        if (roleId == null) {
            return;
        }
        long trustedTenantId = trustedTenantId(tenantId);
        tenantAuthRoutingService.execute(trustedTenantId, () -> {
            List<SysRoleResource> resources = roleResourceMapper.selectEffectiveResourcesByRoleId(roleId);
            roleResourceMapper.delete(new LambdaQueryWrapper<SysRoleResource>()
                    .eq(SysRoleResource::getRoleId, roleId));
            for (SysRoleResource resource : resources) {
                resource.setId(null);
                roleResourceMapper.insert(resource);
            }
            return null;
        });
        refreshRoleCache(trustedTenantId, roleId);
    }

    /**
     * 批量重建指定租户的角色资源。
     *
     * @param tenantId 租户ID
     * @param roleIds 角色ID集合
     */
    public void rebuildRoleResources(Long tenantId, Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            rebuildRoleResource(tenantId, roleId);
        }
    }

    /**
     * 检查用户是否拥有访问指定资源的权限。
     *
     * @param tenantId 租户ID
     * @param roleIds 用户角色ID列表
     * @param httpMethod HTTP方法
     * @param requestPath 请求路径
     * @return 是否允许访问
     */
    public boolean hasPermission(Long tenantId, List<Long> roleIds, String httpMethod, String requestPath) {
        if (roleIds == null || roleIds.isEmpty()) {
            return false;
        }
        long trustedTenantId = trustedTenantId(tenantId);
        for (Long roleId : roleIds) {
            Set<Object> resources = getOrLoadRoleResources(trustedTenantId, roleId);
            for (Object resource : resources) {
                if (matchesResource(String.valueOf(resource), httpMethod, requestPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取缓存中指定租户、指定角色的全部资源。
     *
     * @param tenantId 租户ID
     * @param roleId 角色ID
     * @return 资源集合
     */
    public Set<Object> getRoleResources(Long tenantId, Long roleId) {
        Set<Object> members = redisTemplate.opsForSet().members(cacheKey(tenantId, roleId));
        return members == null ? Collections.emptySet() : members;
    }

    private Set<Object> getOrLoadRoleResources(long tenantId, Long roleId) {
        String key = cacheKey(tenantId, roleId);
        Boolean initialized = redisTemplate.hasKey(key);
        if (!Boolean.TRUE.equals(initialized)) {
            refreshRoleCache(tenantId, roleId);
        }
        return getRoleResources(tenantId, roleId);
    }

    private boolean matchesResource(String entry, String httpMethod, String requestPath) {
        if (EMPTY_MARKER.equals(entry)) {
            return false;
        }
        int separatorIndex = entry.indexOf(':');
        if (separatorIndex < 0) {
            return false;
        }
        String method = entry.substring(0, separatorIndex);
        String pattern = entry.substring(separatorIndex + 1);
        if (!"*".equals(method) && !method.equalsIgnoreCase(httpMethod)) {
            return false;
        }
        return matchesPath(method, pattern, requestPath);
    }

    private String toCacheValue(SysRoleResource resource) {
        if (resource == null || resource.getResourcePath() == null || resource.getResourcePath().isBlank()) {
            return null;
        }
        String method = resource.getHttpMethod();
        if (method == null || method.isBlank()) {
            method = "*";
        }
        return method.trim().toUpperCase() + ":" + resource.getResourcePath().trim();
    }

    private boolean matchesPath(String method, String pattern, String requestPath) {
        if (pathMatcher.match(pattern, requestPath)) {
            return true;
        }
        if (!"*".equals(method) || "/".equals(pattern)) {
            return false;
        }
        String normalizedPattern = pattern.endsWith("/")
                ? pattern.substring(0, pattern.length() - 1)
                : pattern;
        return requestPath.startsWith(normalizedPattern + "/");
    }

    private String cacheKey(Long tenantId, Long roleId) {
        return CACHE_KEY_PREFIX + trustedTenantId(tenantId) + ROLE_RESOURCE_SEGMENT + roleId;
    }

    private long trustedTenantId(Long tenantId) {
        return tenantId == null ? 0L : tenantId;
    }
}
