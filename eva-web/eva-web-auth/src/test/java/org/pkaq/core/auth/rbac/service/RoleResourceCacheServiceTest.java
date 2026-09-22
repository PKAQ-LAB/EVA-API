package org.pkaq.core.auth.rbac.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pkaq.core.auth.rbac.mapper.SysRoleResourceMapper;
import org.pkaq.core.auth.tenant.TenantAuthRoutingService;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 角色资源缓存服务测试。
 *
 * @author PKAQ
 */
@ExtendWith(MockitoExtension.class)
class RoleResourceCacheServiceTest {
    @Mock
    private SysRoleResourceMapper roleResourceMapper;
    @Mock
    private RedisTemplate<Object, Object> redisTemplate;
    @Mock
    private SetOperations<Object, Object> setOperations;
    @Mock
    private EvaConfig evaConfig;
    @Mock
    private TenantAuthRoutingService tenantAuthRoutingService;

    private RoleResourceCacheService service;

    /**
     * 初始化被测服务。
     */
    @BeforeEach
    void setUp() {
        service = new RoleResourceCacheService(
                roleResourceMapper, redisTemplate, evaConfig, tenantAuthRoutingService);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    /**
     * 通配方法的模块基路径仅允许自身及斜杠分隔的后代路径。
     */
    @Test
    void shouldMatchWildcardMethodModulePathWithoutPrefixCollision() {
        when(redisTemplate.hasKey("eva:security:tenant:101:role-resource:7")).thenReturn(true);
        when(setOperations.members("eva:security:tenant:101:role-resource:7"))
                .thenReturn(Set.of("*:/sys/organization"));

        assertTrue(service.hasPermission(101L, List.of(7L), "GET", "/sys/organization/list"));
        assertTrue(service.hasPermission(101L, List.of(7L), "POST", "/sys/organization"));
        assertFalse(service.hasPermission(101L, List.of(7L), "GET", "/sys/organizationExtra/list"));
    }

    /**
     * 具体 HTTP 方法的普通路径不得自动扩大为整个子树。
     */
    @Test
    void shouldNotExpandConcreteMethodPath() {
        when(redisTemplate.hasKey("eva:security:tenant:202:role-resource:8")).thenReturn(true);
        when(setOperations.members("eva:security:tenant:202:role-resource:8"))
                .thenReturn(Set.of("GET:/sys/account"));

        assertTrue(service.hasPermission(202L, List.of(8L), "GET", "/sys/account"));
        assertFalse(service.hasPermission(202L, List.of(8L), "GET", "/sys/account/list"));
        assertFalse(service.hasPermission(202L, List.of(8L), "POST", "/sys/account"));
    }

    /**
     * 相同角色ID在不同租户中必须使用不同缓存键。
     */
    @Test
    void shouldIsolateSameRoleIdByTenant() {
        when(redisTemplate.hasKey("eva:security:tenant:101:role-resource:7")).thenReturn(true);
        when(redisTemplate.hasKey("eva:security:tenant:102:role-resource:7")).thenReturn(true);
        when(setOperations.members("eva:security:tenant:101:role-resource:7"))
                .thenReturn(Set.of("GET:/tenant-a"));
        when(setOperations.members("eva:security:tenant:102:role-resource:7"))
                .thenReturn(Set.of("GET:/tenant-b"));

        assertTrue(service.hasPermission(101L, List.of(7L), "GET", "/tenant-a"));
        assertFalse(service.hasPermission(102L, List.of(7L), "GET", "/tenant-a"));

        verify(setOperations).members("eva:security:tenant:101:role-resource:7");
        verify(setOperations).members("eva:security:tenant:102:role-resource:7");
    }
}
