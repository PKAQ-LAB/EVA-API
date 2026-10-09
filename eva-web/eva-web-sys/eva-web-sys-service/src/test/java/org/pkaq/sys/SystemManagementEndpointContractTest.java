package org.pkaq.sys;

import org.junit.jupiter.api.Test;
import org.pkaq.sys.dict.ctrl.DictCtrl;
import org.pkaq.sys.module.ctrl.ModuleCtrl;
import org.pkaq.sys.notice.ctrl.NoticeCtrl;
import org.pkaq.sys.organization.ctrl.OrganizationCtrl;
import org.pkaq.sys.post.ctrl.PostCtrl;
import org.pkaq.sys.role.ctrl.RoleCtrl;
import org.pkaq.sys.platform.tenant.ctrl.PlatformTenantInspectCtrl;
import org.pkaq.sys.platform.tenant.ctrl.TenantCtrl;
import org.pkaq.sys.platform.tenant.ctrl.TenantPackageCtrl;
import org.pkaq.sys.user.ctrl.UserCtrl;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统管理接口契约测试。
 *
 * @author EVA
 * @date 2026-09-27
 */
class SystemManagementEndpointContractTest {

    @Test
    void platformEntriesRemainSeparateFromInstanceManagement() {
        for (Class<?> controller : Set.of(TenantCtrl.class, TenantPackageCtrl.class,
                PlatformTenantInspectCtrl.class)) {
            assertEquals("org.pkaq.sys.platform.tenant.ctrl", controller.getPackageName());
        }
        for (Class<?> controller : Set.of(ModuleCtrl.class, RoleCtrl.class, DictCtrl.class,
                OrganizationCtrl.class, PostCtrl.class, UserCtrl.class)) {
            assertFalse(controller.getPackageName().startsWith("org.pkaq.sys.platform."));
        }
    }

    @Test
    void exposesSystemManagementCrudAndLinkageEndpoints() {
        assertEndpoints(DictCtrl.class,
                "GET /sys/dictionary/list", "GET /sys/dictionary/get/{id}",
                "POST /sys/dictionary/edit", "POST /sys/dictionary/del/{id}",
                "POST /sys/dictionary/switch");
        assertEndpoints(ModuleCtrl.class,
                "GET /sys/module/list", "GET /sys/module/get/{id}",
                "POST /sys/module/edit", "POST /sys/module/del",
                "POST /sys/module/sort", "POST /sys/module/frozen");
        assertEndpoints(OrganizationCtrl.class,
                "GET /sys/organization/list", "GET /sys/organization/get/{id}",
                "POST /sys/organization/edit", "POST /sys/organization/del",
                "POST /sys/organization/sort", "POST /sys/organization/switch");
        assertEndpoints(PostCtrl.class,
                "GET /sys/post/list", "GET /sys/post/get/{id}",
                "POST /sys/post/edit", "POST /sys/post/del",
                "POST /sys/post/sort", "POST /sys/post/switch");
        assertEndpoints(RoleCtrl.class,
                "GET /sys/role/list", "GET /sys/role/get/{id}",
                "POST /sys/role/edit", "POST /sys/role/del",
                "POST /sys/role/switch", "GET /sys/role/fetchResource",
                "POST /sys/role/grantResource", "GET /sys/role/listUser",
                "POST /sys/role/grantUser");
        assertEndpoints(UserCtrl.class,
                "GET /sys/account/list", "GET /sys/account/get/{id}",
                "POST /sys/account/edit", "POST /sys/account/del",
                "POST /sys/account/switch", "POST /sys/account/grant",
                "POST /sys/account/grantPost", "POST /sys/account/repwd");
        assertEndpoints(TenantCtrl.class,
                "GET /sys/tenant/list", "GET /sys/tenant/get/{id}",
                "POST /sys/tenant/edit", "POST /sys/tenant/del",
                "POST /sys/tenant/switch");
        assertEndpoints(TenantPackageCtrl.class,
                "GET /sys/tenant/package/list", "GET /sys/tenant/package/get/{id}",
                "POST /sys/tenant/package/edit", "POST /sys/tenant/package/del",
                "POST /sys/tenant/package/switch");
        assertEndpoints(NoticeCtrl.class,
                "GET /sys/notice/list", "GET /sys/notice/get/{id}",
                "POST /sys/notice/edit", "POST /sys/notice/del",
                "POST /sys/notice/switch");
        assertEndpoints(PlatformTenantInspectCtrl.class,
                "GET /sys/platform/tenants/options",
                "GET /sys/platform/tenants/{tenantId}/organizations",
                "GET /sys/platform/tenants/{tenantId}/roles");
    }

    private static void assertEndpoints(Class<?> controllerType, String... expectedEndpoints) {
        Set<String> actualEndpoints = collectEndpoints(controllerType);
        Set<String> missingEndpoints = new HashSet<>(Arrays.asList(expectedEndpoints));
        missingEndpoints.removeAll(actualEndpoints);
        assertTrue(missingEndpoints.isEmpty(),
                () -> controllerType.getSimpleName() + " 缺少接口: " + missingEndpoints);
    }

    private static Set<String> collectEndpoints(Class<?> controllerType) {
        Set<String> endpoints = new HashSet<>();
        String rootPath = firstPath(AnnotatedElementUtils.findMergedAnnotation(
                controllerType, RequestMapping.class));
        for (Method method : controllerType.getDeclaredMethods()) {
            RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            if (null == mapping) {
                continue;
            }
            String methodPath = firstPath(mapping);
            for (RequestMethod requestMethod : mapping.method()) {
                endpoints.add(requestMethod.name() + " " + joinPath(rootPath, methodPath));
            }
        }
        return endpoints;
    }

    private static String firstPath(RequestMapping mapping) {
        if (null == mapping) {
            return "";
        }
        String[] paths = mapping.path().length > 0 ? mapping.path() : mapping.value();
        return paths.length > 0 ? paths[0] : "";
    }

    private static String joinPath(String rootPath, String methodPath) {
        String path = rootPath + methodPath;
        return path.startsWith("/") ? path : "/" + path;
    }
}
