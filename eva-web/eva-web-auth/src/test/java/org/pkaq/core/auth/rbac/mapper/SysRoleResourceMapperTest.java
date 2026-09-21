package org.pkaq.core.auth.rbac.mapper;

import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 角色接口资源查询测试。
 *
 * @author PKAQ
 */
class SysRoleResourceMapperTest {

    /**
     * 按钮资源不得转换为通配接口权限，普通租户必须受租户资源上限约束。
     *
     * @throws NoSuchMethodException Mapper 方法不存在
     */
    @Test
    void shouldFilterNonHttpResourcesAndApplyTenantCeiling() throws NoSuchMethodException {
        Method method = SysRoleResourceMapper.class
                .getMethod("selectEffectiveResourcesByRoleId", Long.class);
        String sql = String.join(" ", method.getAnnotation(Select.class).value()).toUpperCase();

        assertTrue(sql.contains("RESOURCE_TYPE) IN ('GET', 'POST', 'PUT', 'DELETE', 'PATCH', '*')"));
        assertTrue(sql.contains("SYS_TENANT_RESOURCE"));
        assertTrue(sql.contains("TENANT_RESOURCE.TENANT_ID = ROLE.TENANT_ID"));
        assertFalse(sql.contains("ELSE '*'"));
    }
}
