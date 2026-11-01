package org.pkaq.sys.dict.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.mybatis.tenant.TenantSchemaResolver;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.pkaq.sys.dict.mapper.DictMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 仅供字典写前完整性校验，不作为用户可见的明细读取入口。
 * @author PKAQ
 * @date 2026-10-31
 */
@Component
@RequiredArgsConstructor
class DictItemSnapshot {
    private final DictMapper mapper;
    private final JdbcTemplate jdbc;
    private final EvaConfig config;
    private final CoreSchemaExecutor core;
    private final TenantSchemaResolver resolver;

    /** 每次入口先按原数据权限锁定可操作主项，仅取其不受owner裁剪的保护行。 */
    Snapshot load(Long mainId) {
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();
        if (mainId == null || mainId <= 0L || user == null || user.getUserId() <= 0L
                || !TransactionSynchronizationManager.isActualTransactionActive()
                || jdbc.getDataSource() == null
                || !(TransactionSynchronizationManager.getResource(jdbc.getDataSource())
                instanceof ConnectionHolder holder)
                || holder.getConnection() == null) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        String schema;
        if (config.isStandaloneMode() && !config.getTenant().isEnable() && user.getTenantId() == 0L) {
            schema = config.getTenant().getStandaloneSchema();
        } else if ((config.isSaasMode() || config.isPlatformMode()) && config.getTenant().isSchemaMode()
                && user.getTenantId() > 0L && Long.valueOf(user.getTenantId()).equals(TenantContext.tenantId())) {
            schema = core.execute(template -> {
                requireSource(config.getTenant().getCoreSchema(), "sys_tenant");
                return resolver.resolve(user.getTenantId());
            });
            if (schema == null || !schema.equals(TenantContext.schemaName())) {
                CommonCodes.PERMISSION_DENY.newException();
            }
        } else {
            CommonCodes.PERMISSION_DENY.newException();
            return null;
        }
        if ((TenantContext.tenantId() != null || TenantContext.schemaName() != null)
                && (!Long.valueOf(user.getTenantId()).equals(TenantContext.tenantId())
                || !schema.equals(TenantContext.schemaName()))) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        requireSource(schema, "sys_dict", "sys_dict_item");
        List<DictEntity> visible = mapper.selectVisible(schema, Wrappers.<DictEntity>lambdaQuery()
                .eq(DictEntity::getId, mainId).eq(DictEntity::getDeleted, 0)
                .eq(DictEntity::getCategory, DictBusinessAccess.BUSINESS).last("FOR UPDATE"));
        DictEntity main = visible.isEmpty() ? null : visible.getFirst();
        if (main == null) {
            SysCodes.RECORD_NOT_FOUND.newException();
        }
        if (main.getFrozen() == FrozenEnumm.READ_ONLY) {
            SysCodes.READ_ONLY_RECORD.newException();
        }
        List<DictItemEntity> items = jdbc.query("""
                SELECT ID, MAIN_ID, D_CODE, D_VALUE, SORT, FROZEN, REMARK
                FROM SYS_DICT_ITEM WHERE MAIN_ID = ? AND DELETED = 0 AND FROZEN = 9999 ORDER BY ID FOR UPDATE
                """, (row, index) -> {
                    DictItemEntity item = new DictItemEntity();
                    item.setId(row.getLong("ID"));
                    item.setMainId(row.getLong("MAIN_ID"));
                    item.setDCode(row.getString("D_CODE"));
                    item.setDValue(row.getString("D_VALUE"));
                    item.setSort(row.getDouble("SORT"));
                    Number frozen = (Number) row.getObject("FROZEN");
                    item.setFrozen(frozen == null ? null : Arrays.stream(FrozenEnumm.values())
                            .filter(value -> value.getCode() == frozen.intValue()).findFirst().orElseThrow());
                    item.setRemark(row.getString("REMARK"));
                    return item;
                }, mainId);
        return new Snapshot(main, items);
    }

    private void requireSource(String schema, String... tables) {
        if (schema == null || !schema.matches("[a-z_][a-z0-9_]{0,62}")) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        for (String table : tables) {
            Map<String, Object> source = jdbc.queryForMap("""
                    SELECT pg_catalog.current_schema() AS current_schema_name,
                        (SELECT n.nspname FROM pg_catalog.pg_class c
                         JOIN pg_catalog.pg_namespace n ON n.oid=c.relnamespace
                         WHERE c.oid=pg_catalog.to_regclass(?)) AS relation_schema_name
                    """, table);
            if (!schema.equals(source.get("current_schema_name"))
                    || !schema.equals(source.get("relation_schema_name"))) {
                CommonCodes.PERMISSION_DENY.newException();
            }
        }
    }

    record Snapshot(DictEntity main, List<DictItemEntity> items) {
    }
}
