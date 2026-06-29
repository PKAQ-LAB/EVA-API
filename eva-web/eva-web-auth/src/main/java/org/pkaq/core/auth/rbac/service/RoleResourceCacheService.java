package org.pkaq.core.auth.rbac.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.auth.rbac.entity.SysRoleResource;
import org.pkaq.core.auth.rbac.mapper.SysRoleResourceMapper;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色资源缓存服务
 * 启动时加载sys_role_resource到Redis
 * 提供基于AntPathMatcher的权限校验
 *
 * @author PKAQ
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleResourceCacheService {

    private static final String CACHE_KEY_PREFIX = "role:resource:";
    private final SysRoleResourceMapper roleResourceMapper;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final EvaConfig evaConfig;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 应用启动后加载所有角色资源到Redis
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadAllRoleResources() {
        if (!evaConfig.getResourcePermission().isEnable()) {
            log.info("资源权限未启用，跳过角色资源权限缓存加载");
            return;
        }
        log.info("开始加载角色资源权限到Redis...");
        Set<Object> oldKeys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (oldKeys != null && !oldKeys.isEmpty()) {
            redisTemplate.delete(oldKeys);
        }

        List<SysRoleResource> all = roleResourceMapper.selectAll();

        Map<Long, List<SysRoleResource>> grouped = all.stream()
                .collect(Collectors.groupingBy(SysRoleResource::getRoleId));

        for (Map.Entry<Long, List<SysRoleResource>> entry : grouped.entrySet()) {
            String key = CACHE_KEY_PREFIX + entry.getKey();
            // 写入 Set<METHOD:PATH>
            Object[] values = entry.getValue().stream()
                    .map(this::toCacheValue)
                    .filter(v -> v != null)
                    .toArray();
            if (values.length > 0) {
                redisTemplate.opsForSet().add(key, values);
            }
        }
        log.info("角色资源权限加载完成, 共 {} 个角色 {} 条记录", grouped.size(), all.size());
    }

    /**
     * 刷新指定角色的资源缓存
     *
     * @param roleId 角色ID
     */
    public void refreshRoleCache(Long roleId) {
        if (roleId == null) {
            return;
        }
        String key = CACHE_KEY_PREFIX + roleId;
        redisTemplate.delete(key);

        List<SysRoleResource> resources = roleResourceMapper.selectByRoleId(roleId);
        if (!resources.isEmpty()) {
            Object[] values = resources.stream()
                    .map(this::toCacheValue)
                    .filter(v -> v != null)
                    .toArray();
            if (values.length > 0) {
                redisTemplate.opsForSet().add(key, values);
            }
        }
    }

    /**
     * 重建指定角色的扁平资源表并刷新缓存。
     *
     * @param roleId 角色ID
     */
    public void rebuildRoleResource(Long roleId) {
        if (roleId == null) {
            return;
        }
        List<SysRoleResource> resources = roleResourceMapper.selectEffectiveResourcesByRoleId(roleId);
        roleResourceMapper.delete(new LambdaQueryWrapper<SysRoleResource>()
                .eq(SysRoleResource::getRoleId, roleId));
        for (SysRoleResource resource : resources) {
            resource.setId(null);
            roleResourceMapper.insert(resource);
        }
        refreshRoleCache(roleId);
    }

    /**
     * 批量重建角色资源。
     *
     * @param roleIds 角色ID集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void rebuildRoleResources(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            rebuildRoleResource(roleId);
        }
    }

    /**
     * 检查用户是否拥有访问指定资源的权限
     *
     * @param roleIds     用户角色ID列表
     * @param httpMethod  HTTP方法 (GET/POST/PUT/DELETE)
     * @param requestPath 请求路径
     * @return true=有权限
     */
    public boolean hasPermission(List<Long> roleIds, String httpMethod, String requestPath) {
        if (roleIds == null || roleIds.isEmpty()) {
            return false;
        }

        for (Long roleId : roleIds) {
            String key = CACHE_KEY_PREFIX + roleId;
            Set<Object> resources = redisTemplate.opsForSet().members(key);
            if (resources == null || resources.isEmpty()) {
                continue;
            }

            for (Object resource : resources) {
                String entry = String.valueOf(resource);
                int idx = entry.indexOf(':');
                if (idx < 0) {
                    continue;
                }
                String method = entry.substring(0, idx);
                String pattern = entry.substring(idx + 1);

                // 方法匹配: * 表示所有方法
                if (!"*".equals(method) && !method.equalsIgnoreCase(httpMethod)) {
                    continue;
                }

                // 路径匹配
                if (pathMatcher.match(pattern, requestPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取缓存中指定角色的所有资源
     *
     * @param roleId 角色ID
     * @return 资源集合
     */
    public Set<Object> getRoleResources(Long roleId) {
        String key = CACHE_KEY_PREFIX + roleId;
        Set<Object> members = redisTemplate.opsForSet().members(key);
        return members != null ? members : Collections.emptySet();
    }

    /**
     * 构造缓存值
     */
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
}
