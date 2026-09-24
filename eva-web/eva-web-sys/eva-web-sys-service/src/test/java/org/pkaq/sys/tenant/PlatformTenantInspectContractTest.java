package org.pkaq.sys.tenant;

import org.junit.jupiter.api.Test;
import org.pkaq.core.constant.PlatformCapabilities;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.sys.tenant.ctrl.PlatformTenantInspectCtrl;
import org.pkaq.sys.tenant.service.PlatformTenantInspectService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 平台跨租户只读接口契约测试。
 *
 * @author PKAQ
 */
class PlatformTenantInspectContractTest {
    @Test
    void exposesOnlyExplicitGetEndpoints() throws Exception {
        assertGet(PlatformTenantInspectCtrl.class.getMethod("options"), "/options");
        assertGet(PlatformTenantInspectCtrl.class.getMethod("organizations", Long.class),
                "/{tenantId}/organizations");
        assertGet(PlatformTenantInspectCtrl.class.getMethod("roles", Long.class),
                "/{tenantId}/roles");
        assertFalse(Arrays.stream(PlatformTenantInspectCtrl.class.getDeclaredMethods())
                .anyMatch(method -> method.isAnnotationPresent(PostMapping.class)));
    }

    @Test
    void serviceQueriesAreReadOnlyTransactions() throws Exception {
        assertReadOnly(PlatformTenantInspectService.class.getMethod("listTenantOptions"));
        assertReadOnly(PlatformTenantInspectService.class.getMethod("listOrganizations", Long.class));
        assertReadOnly(PlatformTenantInspectService.class.getMethod("listRoles", Long.class));
    }

    @Test
    void loginUserModelExposesCapabilities() throws Exception {
        assertNotNull(ThreadUser.class.getMethod("getCapabilities"));
        assertTrue(PlatformCapabilities.TENANT_INSPECT.startsWith("PLATFORM_"));
    }

    private void assertGet(Method method, String path) {
        GetMapping mapping = method.getAnnotation(GetMapping.class);
        assertNotNull(mapping);
        assertArrayEquals(new String[]{path}, mapping.value());
    }

    private void assertReadOnly(Method method) {
        Transactional transactional = method.getAnnotation(Transactional.class);
        assertNotNull(transactional);
        assertTrue(transactional.readOnly());
    }
}
