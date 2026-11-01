package org.pkaq.sys.dict.service;

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
import org.pkaq.sys.dict.cache.DictCacheHelper.Owner;
import org.pkaq.sys.dict.entity.DictEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 字典可信来源、完整标签读取及真实维护资格边界。
 * @author PKAQ
 * @date 2026-11-01
 */
@Component
@RequiredArgsConstructor
class DictBusinessAccess {
    static final String SYSTEM = "SYSTEM";
    static final String BUSINESS = "BUSINESS";
    private final EvaConfig config;
    private final JdbcTemplate jdbc;
    private final CoreSchemaExecutor core;
    private final TenantSchemaResolver resolver;
    private final AntPathMatcher matcher = new AntPathMatcher();

    /** 同一事务内完成一次来源核验与批量投影，不向其它读取路径提供免校验入口。 */
    DictSnapshot snapshotProjection() {
        ThreadUser user = requireSource();
        String local = localSchema(user);
        String system = config.getTenant().isSchemaMode() ? config.getTenant().getCoreSchema() : local;
        List<Owner> owners = List.of(new Owner(system, SYSTEM), new Owner(local, BUSINESS));
        var checkedSchemas = new HashSet<String>();
        var codes = new HashSet<String>();
        Map<String, LinkedHashMap<String, String>> values = new LinkedHashMap<>();
        for (Owner owner : owners) {
            inSource(owner, () -> {
                if (checkedSchemas.add(owner.schema())) {
                    requireDictionaryStructure(owner.schema());
                    Boolean incomplete = jdbc.queryForObject("""
                            SELECT EXISTS (SELECT 1 FROM SYS_DICT WHERE DELETED = 0
                              AND (CATEGORY IS NULL OR CATEGORY NOT IN ('SYSTEM', 'BUSINESS')))
                            """, Boolean.class);
                    if (!Boolean.FALSE.equals(incomplete)) {
                        throw new DictSourceException();
                    }
                }
                Map<Long, LinkedHashMap<String, String>> buckets = new LinkedHashMap<>();
                jdbc.query("""
                        SELECT ID, CODE, FROZEN FROM SYS_DICT WHERE DELETED = 0 AND CATEGORY = ? ORDER BY SORT, ID
                        """, row -> {
                            String code = row.getString("code");
                            // 冻结主项亦参与编码冲突核验，空明细仍形成有效空字典。
                            if (code == null || code.isBlank() || !codes.add(code)) {
                                throw new DictSourceException();
                            }
                            if (row.getInt("frozen") != 1) {
                                LinkedHashMap<String, String> items = new LinkedHashMap<>();
                                buckets.put(row.getLong("id"), items);
                                values.put(code, items);
                            }
                        }, owner.category());
                jdbc.query("""
                        SELECT item.MAIN_ID, item.D_CODE, item.D_VALUE
                        FROM SYS_DICT_ITEM item JOIN SYS_DICT main ON main.ID = item.MAIN_ID
                        WHERE main.DELETED = 0 AND main.CATEGORY = ? AND COALESCE(main.FROZEN, 0) <> 1
                          AND item.DELETED = 0 AND item.FROZEN <> 1
                        ORDER BY main.SORT, main.ID, item.SORT, item.ID
                        """, row -> {
                            Map<String, String> items = buckets.get(row.getLong("main_id"));
                            String key = row.getString("d_code");
                            String label = row.getString("d_value");
                            if (items == null || key == null || key.isBlank() || label == null
                                    || items.containsKey(key)) {
                                throw new DictSourceException();
                            }
                            items.put(key, label);
                        }, owner.category());
                return null;
            });
        }
        return DictSnapshot.create(user, owners, values);
    }

    /** 普通有效主体可读标签；不得以维护资格代替有效主体核验。 */
    List<Owner> sources() {
        ThreadUser user = requireSource();
        String local = localSchema(user);
        String system = config.getTenant().isSchemaMode() ? config.getTenant().getCoreSchema() : local;
        List<Owner> owners = List.of(new Owner(system, SYSTEM), new Owner(local, BUSINESS));
        for (Owner owner : owners) {
            inSource(owner, () -> {
                requireDictionaryStructure(owner.schema());
                return null;
            });
        }
        return owners;
    }

    Owner businessOwner() {
        return sources().get(1);
    }

    /** 临时只使用一个schema，finally恢复调用方search_path，不改ThreadUser/TenantContext。 */
    <T> T inSource(Owner owner, Supplier<T> action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("字典来源读取必须位于事务内");
        }
        String previous = jdbc.queryForObject("SELECT current_setting('search_path')", String.class);
        try {
            jdbc.queryForObject("SELECT pg_catalog.set_config('search_path', ?, true)", String.class, owner.schema());
            return action.get();
        } finally {
            jdbc.queryForObject("SELECT pg_catalog.set_config('search_path', ?, true)", String.class, previous);
        }
    }

    /** 完整标签清单不受个人scope裁剪，仅用于已核验来源的共享只读缓存。 */
    List<DictEntity> completeDicts(Owner owner, String code) {
        return inSource(owner, () -> jdbc.query("""
                SELECT ID, CODE, CATEGORY, FROZEN FROM SYS_DICT
                WHERE DELETED = 0 AND CATEGORY = ?
                  AND (?::varchar IS NULL OR CODE = ?) ORDER BY SORT, ID
                """, (row, index) -> {
                    DictEntity entity = new DictEntity();
                    entity.setId(row.getLong("id"));
                    entity.setCode(row.getString("code"));
                    entity.setCategory(row.getString("category"));
                    entity.setFrozen(row.getInt("frozen") == 1 ? FrozenEnumm.FROZEN : FrozenEnumm.UN_FROZEN);
                    return entity;
                }, owner.category(), code, code));
    }

    LinkedHashMap<String, String> completeItems(Owner owner, Long id) {
        return inSource(owner, () -> {
            LinkedHashMap<String, String> items = new LinkedHashMap<>();
            jdbc.query("""
                    SELECT D_CODE, D_VALUE FROM SYS_DICT_ITEM
                    WHERE MAIN_ID = ? AND DELETED = 0 AND FROZEN <> 1 ORDER BY SORT, ID
                    """, row -> {
                        String key = row.getString("d_code");
                        String value = row.getString("d_value");
                        if (key == null || key.isBlank() || value == null || items.containsKey(key)) {
                            throw new DictSourceException();
                        }
                        items.put(key, value);
                    }, id);
            return items;
        });
    }

    /** 资格来自实际账号和角色关系，不信任缓存角色字符串及ALL数据scope。 */
    boolean businessWritable() {
        sources();
        ThreadUser user = requireSource();
        boolean schemaMode = config.getTenant().isSchemaMode();
        return Boolean.TRUE.equals(jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM SYS_ACCOUNT account
                    JOIN SYS_ROLEUSER_REF membership ON membership.USER_ID = account.ID
                    JOIN SYS_ROLE role ON role.ID = membership.ROLE_ID
                    WHERE account.ID = ? AND account.DELETED = 0 AND account.FROZEN IN (0, 9999)
                      AND role.DELETED = 0 AND role.FROZEN IN (0, 9999) AND role.CODE = ?
                """ + (schemaMode ? ")" : " AND account.TENANT_ID = 0 AND role.TENANT_ID = 0)"),
                Boolean.class, user.getUserId(), schemaMode ? "TENANT_ADMIN" : "ROLE_ADMIN"));
    }

    /** 维护资格始终核验，资源开关只影响操作资源校验。 */
    void requireWrite(String method, String path) {
        sources();
        if (!businessWritable()) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        if (!config.getResourcePermission().isEnable()) {
            return;
        }
        String[] permits = config.getAuth().getPermit();
        if (permits != null && java.util.Arrays.stream(permits).anyMatch(value -> matcher.match(value, path))) {
            return;
        }
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();
        boolean schemaMode = config.getTenant().isSchemaMode();
        List<Long> ids = jdbc.queryForList("""
                SELECT DISTINCT reference.RESOURCE_ID
                FROM SYS_ROLEUSER_REF membership
                JOIN SYS_ROLE role ON role.ID = membership.ROLE_ID
                JOIN SYS_ROLERES_REF reference ON reference.ROLE_ID = role.ID
                WHERE membership.USER_ID = ? AND role.DELETED = 0 AND role.FROZEN IN (0, 9999)
                """ + (schemaMode ? "" : " AND role.TENANT_ID = 0"), Long.class, user.getUserId());
        if (ids.isEmpty() || ids.stream().anyMatch(id -> id == null || id <= 0L)) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        String idList = ids.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        Owner owner = sources().getFirst();
        List<Map<String, Object>> grants = inSource(owner, () -> {
            requireTables(owner.schema(), "sys_module_resources", "sys_module");
            if (schemaMode) {
                requireTables(owner.schema(), "sys_tenant_resource");
            }
            return jdbc.queryForList("""
                    SELECT r.RESOURCE_URL, r.RESOURCE_TYPE
                    FROM SYS_MODULE_RESOURCES r JOIN SYS_MODULE module ON module.ID = r.MAIN_ID
                    WHERE r.DELETED = 0 AND module.DELETED = 0 AND module.FROZEN IN (0, 9999)
                      AND COALESCE(r.TENANT_ID, 0) = 0 AND COALESCE(module.TENANT_ID, 0) = 0
                    """ + " AND r.ID IN (" + idList + ")"
                    + (schemaMode ? " AND EXISTS (SELECT 1 FROM SYS_TENANT_RESOURCE ceiling"
                    + " WHERE ceiling.RESOURCE_ID = r.ID AND ceiling.TENANT_ID = ?)" : ""),
                    schemaMode ? new Object[]{user.getTenantId()} : new Object[0]);
        });
        boolean allowed = grants.stream().anyMatch(row -> row.get("resource_url") instanceof String url
                && row.get("resource_type") instanceof String type
                && ("*".equals(type.trim()) || method.equalsIgnoreCase(type.trim()))
                && matchesPath(type.trim(), url.trim(), path));
        if (!allowed) {
            CommonCodes.PERMISSION_DENY.newException();
        }
    }

    private boolean matchesPath(String method, String pattern, String path) {
        if (matcher.match(pattern, path)) {
            return true;
        }
        String normalized = pattern.endsWith("/") ? pattern.substring(0, pattern.length() - 1) : pattern;
        return "*".equals(method) && !"/".equals(pattern) && path.startsWith(normalized + "/");
    }

    /** 每个实际来源都拒绝未分类有效行，不能被个人scope掩盖。 */
    void requireClassifiedList() {
        for (Owner owner : sources()) {
            inSource(owner, () -> {
                Boolean incomplete = jdbc.queryForObject("""
                        SELECT EXISTS (SELECT 1 FROM SYS_DICT WHERE DELETED = 0
                          AND (CATEGORY IS NULL OR CATEGORY NOT IN ('SYSTEM', 'BUSINESS')))
                        """, Boolean.class);
                if (!Boolean.FALSE.equals(incomplete)) {
                    throw new DictSourceException();
                }
                return null;
            });
        }
    }

    void validateCategory(String category) {
        if (category != null && !SYSTEM.equals(category) && !BUSINESS.equals(category)) {
            CommonCodes.PARAM_ERROR.newException();
        }
    }

    void requireBusiness(DictEntity entity) {
        if (!BUSINESS.equals(entity.getCategory())) {
            CommonCodes.PERMISSION_DENY.newException();
        }
    }

    boolean readOnly(DictEntity entity, boolean writable) {
        return !writable || !BUSINESS.equals(entity.getCategory()) || entity.getFrozen() == FrozenEnumm.READ_ONLY;
    }

    /** 系统保留码不能由个人scope隐藏，也不能与另一来源合并覆盖。 */
    boolean codeExists(Long id, String code) {
        for (Owner owner : sources()) {
            Boolean exists = inSource(owner, () -> jdbc.queryForObject("""
                    SELECT EXISTS (SELECT 1 FROM SYS_DICT WHERE DELETED = 0 AND CATEGORY = ? AND CODE = ?
                      AND (?::bigint IS NULL OR CATEGORY = 'SYSTEM' OR ID <> ?))
                    """, Boolean.class, owner.category(), code, id, id));
            if (Boolean.TRUE.equals(exists)) {
                return true;
            }
        }
        return false;
    }

    void ensureCodeAvailable(Long id, String code) {
        if (codeExists(id, code)) {
            SysCodes.DICT_CODE_EXISTS.newException();
        }
    }

    private ThreadUser requireSource() {
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();
        if (user == null || user.getUserId() <= 0L
                || !TransactionSynchronizationManager.isActualTransactionActive()
                || jdbc.getDataSource() == null
                || !(TransactionSynchronizationManager.getResource(jdbc.getDataSource())
                instanceof ConnectionHolder holder) || holder.getConnection() == null) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        String schema = localSchema(user);
        if ((TenantContext.tenantId() != null || TenantContext.schemaName() != null)
                && (!Objects.equals(user.getTenantId(), TenantContext.tenantId())
                || !schema.equals(TenantContext.schemaName()))) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        if (!schema.equals(jdbc.queryForObject("SELECT current_schema()", String.class))) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        requireTables(schema, "sys_dict", "sys_dict_item", "sys_account", "sys_role",
                "sys_roleuser_ref", "sys_roleres_ref", "sys_account_profile", "sys_organization");
        String tenantClause = config.getTenant().isSchemaMode() ? "" : " AND TENANT_ID = 0";
        Boolean active = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM SYS_ACCOUNT WHERE ID = ?"
                + " AND DELETED = 0 AND FROZEN IN (0, 9999)" + tenantClause + ")",
                Boolean.class, user.getUserId());
        if (!Boolean.TRUE.equals(active)) {
            CommonCodes.PERMISSION_DENY.newException();
        }
        return user;
    }

    private String localSchema(ThreadUser user) {
        String schema;
        if (config.isStandaloneMode() && !config.getTenant().isEnable() && user.getTenantId() == 0L) {
            schema = config.getTenant().getStandaloneSchema();
        } else if ((config.isSaasMode() || config.isPlatformMode())
                && config.getTenant().isSchemaMode() && user.getTenantId() > 0L) {
            schema = core.execute(template -> {
                requireTables(config.getTenant().getCoreSchema(), "sys_tenant");
                return resolver.resolve(user.getTenantId());
            });
        } else {
            throw new DictSourceException();
        }
        if (schema == null || !schema.matches("[a-z_][a-z0-9_]{0,62}")) {
            throw new DictSourceException();
        }
        return schema;
    }

    private void requireTables(String schema, String... tables) {
        if (schema == null || !schema.matches("[a-z_][a-z0-9_]{0,62}")) {
            throw new DictSourceException();
        }
        for (String table : tables) {
            String namespace = jdbc.queryForObject("""
                    SELECT (SELECT n.nspname FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n
                    ON n.oid = c.relnamespace WHERE c.oid = pg_catalog.to_regclass(?))
                    """, String.class, table);
            if (!schema.equals(namespace)) {
                throw new DictSourceException();
            }
        }
    }

    private void requireDictionaryStructure(String schema) {
        requireTables(schema, "sys_dict", "sys_dict_item");
        for (String table : List.of("sys_dict", "sys_dict_item")) {
            List<String> expected = new ArrayList<>(List.of("id", "revision", "deleted", "frozen", "sort",
                    "create_id", "create_by", "utc_create", "modify_id", "modify_by", "utc_modify", "remark"));
            expected.addAll("sys_dict".equals(table) ? List.of("type", "code", "name", "category")
                    : List.of("main_id", "d_code", "d_value"));
            List<String> columns = jdbc.queryForList("""
                    SELECT column_name FROM information_schema.columns WHERE table_schema = ? AND table_name = ?
                    """, String.class, schema, table);
            if (!columns.containsAll(expected)) {
                throw new DictSourceException();
            }
        }
        Boolean category = jdbc.queryForObject("""
                SELECT EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_schema = ? AND table_name = 'sys_dict' AND column_name = 'category'
                  AND data_type = 'character varying' AND character_maximum_length = 16)
                """, Boolean.class, schema);
        List<String> definitions = jdbc.queryForList("""
                SELECT pg_catalog.pg_get_constraintdef(oid) FROM pg_catalog.pg_constraint
                WHERE conrelid = pg_catalog.to_regclass('sys_dict')
                  AND conname = 'ck_sys_dict_category' AND contype = 'c' AND convalidated
                """, String.class);
        String definition = definitions.size() == 1 ? definitions.getFirst()
                .replaceAll("::(?:character varying|text)(?:\\[\\])?", "")
                .replaceAll("[\\s()]", "") : "";
        if (!Boolean.TRUE.equals(category)
                || !"CHECKcategoryISNULLORcategory=ANYARRAY['SYSTEM','BUSINESS']".equals(definition)) {
            throw new DictSourceException();
        }
    }
}
