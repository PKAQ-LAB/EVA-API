package org.pkaq.sys.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.constant.PlatformCapabilities;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mybatis.tenant.TargetTenantExecutor;
import org.pkaq.core.mybatis.util.TreeHelper;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.sys.organization.bo.OrganizationQueryBo;
import org.pkaq.sys.organization.convert.OrganizationConvert;
import org.pkaq.sys.organization.mapper.OrganizationMapper;
import org.pkaq.sys.organization.vo.OrganizationListVo;
import org.pkaq.sys.role.convert.RoleConvert;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.role.mapper.RoleMapper;
import org.pkaq.sys.role.vo.RoleListVo;
import org.pkaq.sys.tenant.entity.TenantEntity;
import org.pkaq.sys.tenant.mapper.TenantMapper;
import org.pkaq.sys.tenant.vo.PlatformTenantOptionVo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 平台管理员跨租户只读查看服务。
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "eva", name = "mode", havingValue = "platform")
public class PlatformTenantInspectService {
    private final EvaConfig evaConfig;
    private final TenantMapper tenantMapper;
    private final OrganizationMapper organizationMapper;
    private final OrganizationConvert organizationConvert;
    private final RoleMapper roleMapper;
    private final RoleConvert roleConvert;
    private final TargetTenantExecutor targetTenantExecutor;

    /**
     * 查询当前可查看的有效租户选项。
     */
    @Transactional(readOnly = true)
    public List<PlatformTenantOptionVo> listTenantOptions() {
        assertPlatformInspector();
        Date now = new Date();
        return this.tenantMapper.selectList(new LambdaQueryWrapper<TenantEntity>()
                        .ne(TenantEntity::getFrozen, FrozenEnumm.FROZEN)
                        .and(wrapper -> wrapper.isNull(TenantEntity::getExpirationDate)
                                .or().gt(TenantEntity::getExpirationDate, now))
                        .orderByAsc(TenantEntity::getName)
                        .orderByAsc(TenantEntity::getId))
                .stream()
                .map(tenant -> new PlatformTenantOptionVo(tenant.getId(), tenant.getCode(), tenant.getName()))
                .toList();
    }

    /**
     * 查询指定租户的组织树。
     */
    @Transactional(readOnly = true)
    public Collection<OrganizationListVo> listOrganizations(Long targetTenantId) {
        assertTargetTenantAvailable(targetTenantId);
        return this.targetTenantExecutor.execute(targetTenantId, () -> {
            List<OrganizationListVo> organizations = this.organizationConvert.entityToListVo(
                    this.organizationMapper.selectOrgMapList(new OrganizationQueryBo()));
            return TreeHelper.buildTree(organizations);
        });
    }

    /**
     * 查询指定租户的角色列表。
     */
    @Transactional(readOnly = true)
    public List<RoleListVo> listRoles(Long targetTenantId) {
        assertTargetTenantAvailable(targetTenantId);
        return this.targetTenantExecutor.execute(targetTenantId, () -> this.roleConvert.toListVo(
                this.roleMapper.selectList(new LambdaQueryWrapper<RoleEntity>()
                        .orderByAsc(RoleEntity::getSort)
                        .orderByAsc(RoleEntity::getId))));
    }

    private void assertTargetTenantAvailable(Long targetTenantId) {
        assertPlatformInspector();
        if (targetTenantId == null || targetTenantId <= 0L) {
            throw new SecurityException("目标租户非法");
        }
        TenantEntity tenant = this.tenantMapper.selectById(targetTenantId);
        boolean unavailable = tenant == null
                || tenant.getFrozen() == FrozenEnumm.FROZEN
                || tenant.getExpirationDate() != null && !tenant.getExpirationDate().after(new Date());
        if (unavailable) {
            throw new SecurityException("目标租户不可用");
        }
    }

    private void assertPlatformInspector() {
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();
        boolean allowed = evaConfig.isPlatformMode()
                && user != null
                && Objects.equals(user.getTenantId(), 0L)
                && ThreadUserHelper.hasCapability(PlatformCapabilities.TENANT_INSPECT);
        if (!allowed) {
            throw new SecurityException("无平台跨租户查看权限");
        }
    }
}
