package org.pkaq.sys.tenant.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.TenantProperties;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.pkaq.sys.tenant.convert.TenantConvert;
import org.pkaq.sys.tenant.mapper.TenantAuthorizationMapper;
import org.pkaq.sys.tenant.mapper.TenantMapper;
import org.pkaq.sys.tenant.mapper.TenantResourceMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 独立验证租户控制面在非平台 schema 模式下拒绝所有业务入口。
 *
 * @author Codex
 * @date 2026-10-08
 */
class TenantModeBoundaryReviewTest {

    /** 拒绝必须发生在任何查询、写入、事件或 schema 操作之前。 */
    @ParameterizedTest
    @ValueSource(strings = {"standalone", "saas", "platform"})
    void rejectsAllEntriesBeforeStorage(String mode) {
        EvaConfig config = new EvaConfig();
        config.setMode(mode);
        TenantProperties tenant = new TenantProperties();
        tenant.setEnable(!"platform".equals(mode));
        tenant.setMode(TenantProperties.MODE_SCHEMA);
        config.setTenant(tenant);

        TenantMapper mapper = mock(TenantMapper.class);
        TenantResourceMapper resources = mock(TenantResourceMapper.class);
        TenantAuthorizationMapper authorizations = mock(TenantAuthorizationMapper.class);
        RoleResourceMapper roles = mock(RoleResourceMapper.class);
        TenantConvert convert = mock(TenantConvert.class);
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        TenantSchemaProvisioner provisioner = mock(TenantSchemaProvisioner.class);
        TenantLocalAdminService administrator = mock(TenantLocalAdminService.class);
        TenantPrivateAccountService accounts = mock(TenantPrivateAccountService.class);
        TenantService service = new TenantService(resources, authorizations, roles, convert, events,
                provisioner, administrator, accounts, config);
        ReflectionTestUtils.setField(service, "mapper", mapper);

        assertThrows(BizException.class, () -> service.switchFrozen(null));
        assertThrows(BizException.class, () -> service.delete(null));
        assertThrows(BizException.class, () -> service.edit(null));
        assertThrows(BizException.class, () -> service.get(1L));
        assertThrows(BizException.class, () -> service.listPage(null));
        assertThrows(BizException.class, () -> service.checkUnique(null));
        verifyNoInteractions(mapper, resources, authorizations, roles, convert, events,
                provisioner, administrator, accounts);
    }
}
