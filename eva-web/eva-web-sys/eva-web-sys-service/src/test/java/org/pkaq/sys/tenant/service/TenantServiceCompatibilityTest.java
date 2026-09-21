package org.pkaq.sys.tenant.service;

import org.junit.jupiter.api.Test;
import org.pkaq.sys.tenant.bo.TenantAoeBo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 租户授权请求兼容模式测试。
 *
 * @author PKAQ
 */
class TenantServiceCompatibilityTest {

    @Test
    void keepsLegacyResourceModeWhenNewGrantFieldsAreAbsent() {
        TenantAoeBo bo = new TenantAoeBo();
        bo.setResourceIds(List.of(1L));

        assertTrue(TenantService.usesLegacyResourceMode(bo));
    }

    @Test
    void switchesToDerivedModeWhenEitherNewGrantFieldIsExplicit() {
        TenantAoeBo roleRequest = new TenantAoeBo();
        roleRequest.setRoleIds(List.of());
        TenantAoeBo packageRequest = new TenantAoeBo();
        packageRequest.setPackageIds(List.of());

        assertFalse(TenantService.usesLegacyResourceMode(roleRequest));
        assertFalse(TenantService.usesLegacyResourceMode(packageRequest));
    }
}
