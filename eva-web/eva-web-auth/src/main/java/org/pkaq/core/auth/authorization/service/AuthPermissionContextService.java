package org.pkaq.core.auth.authorization.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.auth.spi.IPermissionSnapshotQuery;
import org.pkaq.core.auth.spi.IAccountProfileQuery;
import org.pkaq.core.auth.spi.ITenantAuthRouter;
import org.pkaq.core.auth.spi.model.AccountSnapshot;
import org.pkaq.core.auth.spi.model.AccountProfileSnapshot;
import org.pkaq.core.auth.spi.model.RoleSnapshot;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.constant.PlatformCapabilities;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.util.StrUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 根据已认证的账号构建本次请求的权限上下文。
 *
 * @author PKAQ
 * @date 2026-10-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthPermissionContextService {
    private final EvaConfig evaConfig;
    private final IPermissionSnapshotQuery permissionQuery;
    private final ITenantAuthRouter tenantRouter;
    private final IAccountProfileQuery profileQuery;

    /**
     * 仅管理权限应用加载可选管理资料，纯认证不读取资料表。
     * @param accountId 账号编号
     * @param tenantId 可信租户编号
     * @return 可选资料
     */
    public AccountProfileSnapshot findProfile(long accountId, long tenantId) {
        if (!isPermissionContextRequired()) {
            return null;
        }
        return evaConfig.getTenant().isSchemaMode()
                ? tenantRouter.execute(tenantId, () -> profileQuery.findProfile(accountId))
                : profileQuery.findProfile(accountId);
    }

    private boolean isPermissionContextRequired() {
        return evaConfig.getResourcePermission().isEnable() || evaConfig.getDataPermission().isEnable()
                || evaConfig.isPlatformMode();
    }

    /**
     * 仅在启用功能权限、数据权限或平台能力时读取角色。
     * @param userId 用户编号
     * @param tenantId 可信租户编号
     * @return 角色快照
     */
    public List<RoleSnapshot> findRoles(long userId, long tenantId) {
        if (!isPermissionContextRequired()) {
            return List.of();
        }
        List<RoleSnapshot> roles = evaConfig.getTenant().isSchemaMode()
                ? tenantRouter.execute(tenantId, () -> permissionQuery.findRoles(userId))
                : permissionQuery.findRoles(userId);
        return null == roles ? List.of() : roles;
    }

    /**
     * 构建当前用户快照，数据范围仅在数据权限启用时解析。
     *
     * @param userId 用户编号
     * @param tenantId 租户编号
     * @param account 登录账号
     * @param authState 可信服务端状态
     * @return 请求上下文
     */
    public ThreadUser buildUser(long userId, long tenantId, String account, AccountSnapshot authState) {
        List<RoleSnapshot> roles = findRoles(userId, tenantId);
        AccountProfileSnapshot profile = findProfile(userId, tenantId);
        Map<Long, ThreadUser.GrantedRoles> rolesMap = roles.stream()
                .filter(Objects::nonNull)
                .filter(role -> null != role.getId())
                .collect(Collectors.toMap(RoleSnapshot::getId,
                        role -> new ThreadUser.GrantedRoles(role.getName(), role.getCode()),
                        (first, second) -> first, LinkedHashMap::new));
        boolean platformAdmin = evaConfig.isPlatformMode() && 0L == tenantId
                && rolesMap.values().stream()
                .anyMatch(role -> CommonConstant.ADMIN_ROLE_NAME.equals(role.getCode()));
        List<ThreadUser.DataScope> dataScopes = evaConfig.getDataPermission().isEnable()
                ? roles.stream().filter(Objects::nonNull)
                .map(role -> new ThreadUser.DataScope(role.getDataScope(), parseOrgIds(role.getDataOrgIds()))).toList()
                : List.of();
        return new ThreadUser().setUserId(userId).setTenantId(tenantId).setAccount(account)
                .setName(null != profile && StrUtils.isNotBlank(profile.getName()) ? profile.getName()
                        : StrUtils.isNotBlank(authState.getNickName()) ? authState.getNickName() : account)
                .setDeptId(null == profile || null == profile.getDeptId() ? 0L : profile.getDeptId())
                .setRoles(rolesMap.keySet().stream().map(String::valueOf).toArray(String[]::new))
                .setRolesMap(rolesMap).setDataScopes(dataScopes)
                .setCapabilities(platformAdmin ? Set.of(PlatformCapabilities.TENANT_INSPECT) : Set.of());
    }

    private List<Long> parseOrgIds(String value) {
        if (null == value || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(",")).map(String::trim).filter(part -> !part.isEmpty())
                .map(this::parsePositiveLong).filter(Objects::nonNull).distinct().toList();
    }

    private Long parsePositiveLong(String value) {
        try {
            long identifier = Long.parseLong(value);
            return identifier > 0L ? identifier : null;
        } catch (NumberFormatException exception) {
            log.warn("忽略非法的数据权限组织 ID: {}", value);
            return null;
        }
    }
}
