package org.pkaq.sys.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.mybatis.tenant.TenantSchemaResolver;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.mybatis.tenant.TenantSchema;
import org.pkaq.core.util.CollUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.role.mapper.RoleMapper;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.user.bo.UserGrantBo;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.pkaq.sys.user.vo.AssignedRoleSummaryVo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户角色关系服务
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class UserRoleRefSerivce implements IUserRoleRefSerivce {
    /** 系统内置管理员账号编码。 */
    private static final String SYSTEM_ADMIN_CODE = "9999";

    /** 详情及数据权限子查询涉及的固定表，全部必须来自同一可信schema。 */
    private static final Set<String> DETAIL_TABLES = Set.of("sys_account", "sys_account_profile",
            "sys_roleuser_ref", "sys_role", "sys_organization", "sys_postuser_ref");

    private final RoleUserMapper roleUserMapper;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;
    private final JdbcTemplate jdbcTemplate;
    private final EvaConfig evaConfig;
    private final TenantSchemaResolver tenantSchemaResolver;
    private final CoreSchemaExecutor coreSchemaExecutor;

    /**
     * 在调用方详情事务中读取可见账号及同一次已授关系快照。
     * @param userId 被查看账号ID
     * @return 原关系和当前操作者可读的最小角色摘要
     */
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    public UserRolesDetail readUserRoles(Long userId) {
        requireDetailSource();
        UserEntity user = userId == null || userId <= 0L ? null : this.userMapper.selectById(userId);
        if (user == null) {
            SysCodes.CANNOT_FIND_USER.newException();
        }
        List<Long> original = this.roleUserMapper.selectRoleIds(userId);
        Set<Long> ids = this.sanitizeIds(original);
        List<AssignedRoleSummaryVo> summaries = ids.isEmpty() ? List.of() : this.roleMapper.selectMaps(
                new LambdaQueryWrapper<RoleEntity>().select(RoleEntity::getId, RoleEntity::getName,
                        RoleEntity::getCode, RoleEntity::getFrozen).in(RoleEntity::getId, ids))
                .stream().map(role -> {
                    AssignedRoleSummaryVo summary = new AssignedRoleSummaryVo();
                    // 原实体setter会改写历史code且丢失NULL，摘要只映射数据库原四字段。
                    summary.setId(Long.valueOf(role.get("id").toString()));
                    summary.setName((String) role.get("name"));
                    summary.setCode((String) role.get("code"));
                    Integer state = role.get("frozen") == null ? null
                            : Integer.valueOf(role.get("frozen").toString());
                    if (state != null && !Set.of(0, 1, 9999).contains(state)) {
                        throw new IllegalStateException("角色状态无法识别");
                    }
                    summary.setFrozen(state);
                    return summary;
                }).toList();
        return new UserRolesDetail(user, original, summaries);
    }

    /** 同一次读取的内部结果，不作为HTTP实体直接暴露。 */
    public record UserRolesDetail(UserEntity user, List<Long> roleIds, List<AssignedRoleSummaryVo> assignedRoles) {
    }

    /**
     * 保存用户角色关系。
     *
     * @param bo 用户授权参数
     */
    @TenantSchema
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void saveRoles(UserGrantBo bo) {
        if (bo == null || bo.getUserId() == null) {
            SysCodes.CANNOT_FIND_USER.newException();
            return;
        }

        Long userId = bo.getUserId();
        this.ensureUserExists(userId);

        Set<Long> incoming = this.sanitizeIds(bo.getRoleIds());
        this.ensureRolesUsable(incoming);

        Set<Long> existing = this.roleUserMapper.selectList(
                        new LambdaQueryWrapper<RoleUserEntity>()
                                .select(RoleUserEntity::getRoleId)
                                .eq(RoleUserEntity::getUserId, userId))
                .stream()
                .map(RoleUserEntity::getRoleId)
                .collect(Collectors.toSet());

        Set<Long> toInsert = new HashSet<>(incoming);
        toInsert.removeAll(existing);
        Set<Long> toDelete = new HashSet<>(existing);
        toDelete.removeAll(incoming);

        if (!toDelete.isEmpty()) {
            this.roleUserMapper.delete(new LambdaQueryWrapper<RoleUserEntity>()
                    .eq(RoleUserEntity::getUserId, userId)
                    .in(RoleUserEntity::getRoleId, toDelete));
        }
        if (!toInsert.isEmpty()) {
            for (Long roleId : toInsert) {
                RoleUserEntity ref = new RoleUserEntity();
                ref.setRoleId(roleId);
                ref.setUserId(userId);
                this.roleUserMapper.insert(ref);
            }
        }

        if (!toInsert.isEmpty() || !toDelete.isEmpty()) {
            this.userMapper.incrementPermVer(userId);
        }
    }

    /** 防止search_path正确但必要表从core或第三schema回退，身份和来源不随数据开关关闭。 */
    private void requireDetailSource() {
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();
        if (user == null || user.getUserId() <= 0L
                || !TransactionSynchronizationManager.isActualTransactionActive()
                || this.jdbcTemplate.getDataSource() == null
                || !(TransactionSynchronizationManager.getResource(this.jdbcTemplate.getDataSource())
                        instanceof ConnectionHolder holder) || holder.getConnection() == null) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        String expected;
        if (this.evaConfig.isStandaloneMode() && !this.evaConfig.getTenant().isEnable()) {
            expected = this.evaConfig.getTenant().getStandaloneSchema();
            if (user.getTenantId() != 0L) {
                CommonCodes.PERMISSION_DENY.newException();
            }
        } else if ((this.evaConfig.isSaasMode() || this.evaConfig.isPlatformMode())
                && this.evaConfig.getTenant().isSchemaMode() && user.getTenantId() > 0L) {
            if (!Long.valueOf(user.getTenantId()).equals(TenantContext.tenantId())
                    || TenantContext.schemaName() == null) {
                CommonCodes.PERMISSION_DENY.newException();
            }
            expected = resolveDetailSchema(user.getTenantId());
        } else {
            CommonCodes.PERMISSION_DENY.newException();
            return;
        }
        if (expected == null || !expected.matches("[a-z_][a-z0-9_]{0,62}")
                || ((TenantContext.tenantId() != null || TenantContext.schemaName() != null)
                && (!Long.valueOf(user.getTenantId()).equals(TenantContext.tenantId())
                || !expected.equals(TenantContext.schemaName())))) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        List<Map<String, Object>> sources = this.jdbcTemplate.queryForList("""
                SELECT pg_catalog.current_schema() AS current_schema_name, r.name AS relation_name,
                    n.nspname AS relation_schema_name
                FROM (VALUES (?), (?), (?), (?), (?), (?)) AS r(name)
                LEFT JOIN pg_catalog.pg_class c ON c.oid = pg_catalog.to_regclass(r.name)
                LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                """, "sys_account", "sys_account_profile", "sys_roleuser_ref", "sys_role",
                "sys_organization", "sys_postuser_ref");
        Set<String> names = new HashSet<>();
        for (Map<String, Object> source : sources) {
            if (!expected.equals(source.get("current_schema_name"))
                    || !expected.equals(source.get("relation_schema_name"))) {
                CommonCodes.PERMISSION_DENY.newException();
            }
            names.add((String) source.get("relation_name"));
        }
        if (sources.size() != DETAIL_TABLES.size() || !names.equals(DETAIL_TABLES)) {
            CommonCodes.PERMISSION_DENY.newException();
        }
    }

    /** 局部复核权威映射表实际来自core，拒绝临时表或同名表遮蔽，不修改全局resolver。 */
    private String resolveDetailSchema(long tenantId) {
        String core = this.evaConfig.getTenant().getCoreSchema();
        if (core == null || !core.matches("[a-z_][a-z0-9_]{0,62}")) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        return this.coreSchemaExecutor.execute(template -> {
            Map<String, Object> source = template.queryForMap("""
                    SELECT pg_catalog.current_schema() AS control_schema_name,
                        (SELECT n.nspname FROM pg_catalog.pg_class c
                            JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                            WHERE c.oid = pg_catalog.to_regclass(?)) AS control_relation_schema
                    """, "sys_tenant");
            if (!core.equals(source.get("control_schema_name"))
                    || !core.equals(source.get("control_relation_schema"))) {
                CommonCodes.PERMISSION_DENY.newException();
            }
            // 执行器在finally恢复原事务search_path，不读取core账号、关系或角色。
            return this.tenantSchemaResolver.resolve(tenantId);
        });
    }

    private void ensureUserExists(Long userId) {
        UserEntity user = this.userMapper.selectById(userId);
        if (user == null) {
            SysCodes.CANNOT_FIND_USER.newException();
            return;
        }
        if (user.getFrozen() == FrozenEnumm.READ_ONLY || SYSTEM_ADMIN_CODE.equals(user.getCode())) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
    }

    private void ensureRolesUsable(Set<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return;
        }

        List<RoleEntity> roles = this.roleMapper.selectList(new LambdaQueryWrapper<RoleEntity>()
                .select(RoleEntity::getId)
                .in(RoleEntity::getId, roleIds)
                .ne(RoleEntity::getFrozen, FrozenEnumm.FROZEN));
        if (roles.size() != roleIds.size()) {
            SysCodes.RECORD_NOT_FOUND.newException();
        }
    }

    private Set<Long> sanitizeIds(List<Long> ids) {
        if (CollUtils.isEmpty(ids)) {
            return Collections.emptySet();
        }
        return ids.stream()
                .filter(id -> id != null && id > 0L)
                .collect(Collectors.toSet());
    }
}
