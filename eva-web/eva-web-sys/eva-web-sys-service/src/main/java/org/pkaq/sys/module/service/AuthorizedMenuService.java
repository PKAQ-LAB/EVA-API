package org.pkaq.sys.module.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.mybatis.tenant.TenantSchemaRouter;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.sys.module.mapper.AuthorizedMenuMapper;
import org.pkaq.sys.module.mapper.AuthorizedMenuRow;
import org.pkaq.sys.module.vo.ModuleMenuVo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 当前认证用户的菜单元数据边界，不改变业务查询的数据范围。
 * @author PKAQ
 * @date 2026-10-28
 */
@Service
@RequiredArgsConstructor
public class AuthorizedMenuService {
    private final AuthorizedMenuMapper mapper;
    private final EvaConfig config;
    private final TenantSchemaRouter router;
    private final CoreSchemaExecutor coreExecutor;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 从认证上下文取得身份，并仅返回有效授权资源对应的菜单。
     * @return 当前用户菜单树
     */
    @Transactional(readOnly = true)
    public Collection<ModuleMenuVo> fetchCurrentMenus() {
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();
        if (null == user || user.getUserId() <= 0L
                || !TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            throw new SecurityException("菜单读取缺少可信身份或只读事务");
        }
        Long previousTenant = TenantContext.tenantId();
        String previousSchema = TenantContext.schemaName();
        if ((null != previousTenant || null != previousSchema)
                && (!Objects.equals(previousTenant, user.getTenantId()) || null == previousSchema)) {
            throw new SecurityException("菜单租户上下文冲突");
        }
        if (!config.getTenant().isSchemaMode()) {
            requireStandaloneSource(user, previousSchema);
            return loadMenus(user, false);
        }
        if (user.getTenantId() <= 0L) {
            throw new SecurityException("菜单读取缺少可信租户");
        }
        String previousPath = null;
        try {
            previousPath = router.routeCurrentTransaction(user.getTenantId());
            if (!Objects.equals(TenantContext.tenantId(), user.getTenantId())
                    || null == TenantContext.schemaName()
                    || (null != previousSchema && !previousSchema.equals(TenantContext.schemaName()))) {
                throw new SecurityException("菜单租户路由不一致");
            }
            requireTableSources(TenantContext.schemaName(),
                    List.of("sys_roleuser_ref", "sys_role", "sys_roleres_ref"));
            return loadMenus(user, true);
        } finally {
            try {
                if (null != previousPath) {
                    router.restoreCurrentTransaction(previousPath);
                }
            } finally {
                if (null == previousTenant && null == previousSchema) {
                    TenantContext.clear();
                } else {
                    TenantContext.bind(previousTenant, previousSchema);
                }
            }
        }
    }

    private Collection<ModuleMenuVo> loadMenus(ThreadUser user, boolean schemaMode) {
        Set<Long> resourceIds = mapper.selectCurrentResourceIds(user.getUserId(), !schemaMode);
        if (null == resourceIds || resourceIds.isEmpty()) {
            return List.of();
        }
        if (resourceIds.stream().anyMatch(resourceId -> null == resourceId || resourceId <= 0L)) {
            throw new SecurityException("菜单授权资源编号非法");
        }
        List<AuthorizedMenuRow> rows = schemaMode
                ? coreExecutor.execute(template -> mapper.selectMenuRows(resourceIds, user.getTenantId(), true))
                : mapper.selectMenuRows(resourceIds, 0L, false);
        return buildTree(rows);
    }

    private void requireStandaloneSource(ThreadUser user, String previousSchema) {
        String schema = config.getTenant().getStandaloneSchema();
        if (!config.isStandaloneMode() || config.getTenant().isEnable() || 0L != user.getTenantId()
                || null == schema || !schema.matches("[a-z_][a-z0-9_]{0,62}")
                || (null != previousSchema && !schema.equals(previousSchema))
                || null == jdbcTemplate.getDataSource()
                || !TransactionSynchronizationManager.hasResource(jdbcTemplate.getDataSource())) {
            throw new SecurityException("独立菜单数据源边界非法");
        }
        String currentSchema = jdbcTemplate.queryForObject("SELECT current_schema()", String.class);
        if (!schema.equals(currentSchema)) {
            throw new SecurityException("独立菜单 schema 不一致");
        }
        // 核对实际解析来源，禁止 search_path 从其它 schema 补齐授权表。
        requireTableSources(schema, List.of("sys_roleuser_ref", "sys_role", "sys_roleres_ref",
                "sys_module", "sys_module_resources"));
    }

    private void requireTableSources(String schema, List<String> tables) {
        for (String table : tables) {
            String namespace = jdbcTemplate.queryForObject("""
                    SELECT n.nspname FROM pg_catalog.pg_class c
                    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                    WHERE c.oid = pg_catalog.to_regclass(?)
                    """, String.class, table);
            if (!schema.equals(namespace)) {
                throw new SecurityException("独立菜单授权表来源不一致");
            }
        }
    }

    private Collection<ModuleMenuVo> buildTree(List<AuthorizedMenuRow> rows) {
        Map<Long, ModuleMenuVo> modules = new LinkedHashMap<>();
        Map<Long, Set<Long>> resourceKeys = new LinkedHashMap<>();
        for (AuthorizedMenuRow row : rows) {
            ModuleMenuVo module = row.getModule();
            if (null == module || null == module.getId() || null == row.getResource()
                    || null == row.getResource().getId()) {
                throw new SecurityException("菜单授权投影不完整");
            }
            ModuleMenuVo target = modules.computeIfAbsent(module.getId(), moduleId -> {
                module.setResources(new ArrayList<>());
                module.setChildren(null);
                return module;
            });
            if (resourceKeys.computeIfAbsent(module.getId(), moduleId -> new HashSet<>())
                    .add(row.getResource().getId())) {
                target.getResources().add(row.getResource());
            }
        }
        List<ModuleMenuVo> roots = new ArrayList<>();
        for (ModuleMenuVo module : modules.values()) {
            Set<Long> ancestors = new HashSet<>();
            ModuleMenuVo current = module;
            while (null != current) {
                if (!ancestors.add(current.getId())) {
                    throw new SecurityException("菜单父子关联存在循环");
                }
                current = modules.get(current.getPid());
            }
            ModuleMenuVo parent = modules.get(module.getPid());
            if (null == parent) {
                roots.add(module);
            } else {
                if (null == parent.getChildren()) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(module);
            }
        }
        return roots;
    }
}
