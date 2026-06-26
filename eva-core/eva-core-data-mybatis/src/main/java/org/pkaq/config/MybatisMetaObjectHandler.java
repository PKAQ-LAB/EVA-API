package org.pkaq.config;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.pkaq.core.enums.DelEnumm;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Mybatis Plus 自动填充策略实现类
 * <p>
 * tenantId 自动填充策略：
 * - 仅在新增时填充
 * - 取 ThreadUserHelper.getTenantId()（匿名请求时为 null，跳过填充）
 * - 与 CustomTenantLineHandler 共用 eva.tenant.ignoreTables 配置，
 *   被忽略的表（如 sys_tenant、sys_module 等系统级表）不填 tenantId
 *
 * @author PKAQ
 */
@Slf4j
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {
    private static final String ANONYMOUS = "Anonymous";

    /** 通过字段注入避免在多租户拦截器/EvaConfig 之间形成构造循环 */
    @Autowired
    private EvaConfig evaConfig;

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("#EVA-DEBUG....自动填充Insert字段");
        var uid = Optional.of(ThreadUserHelper.getUserId()).orElse(-1L);
        var uname = Optional.ofNullable(ThreadUserHelper.getUserName()).orElse(ANONYMOUS);

        this.strictInsertFill(metaObject, "createId", Long.class, uid);
        this.strictInsertFill(metaObject, "createBy", String.class, uname);
        this.strictInsertFill(metaObject, "utcCreate", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "sort", Double.class, 0d);
        this.strictInsertFill(metaObject, "frozen", Integer.class, FrozenEnumm.UN_FROZEN.getCode());
        this.strictInsertFill(metaObject, "deleted", Integer.class, DelEnumm.UN_DELETED.getCode());

        // tenantId 自动填充
        Long currentTid = ThreadUserHelper.getTenantId();
        if (currentTid != null && shouldFillTenantId(metaObject)) {
            this.strictInsertFill(metaObject, "tenantId", Long.class, currentTid);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("#EVA-DEBUG....自动填充Update字段");
        var uid = Optional.of(ThreadUserHelper.getUserId()).orElse(-1L);
        var uname = Optional.ofNullable(ThreadUserHelper.getUserName()).orElse(ANONYMOUS);

        this.strictInsertFill(metaObject, "modifyId", Long.class, uid);
        this.strictUpdateFill(metaObject, "modifyBy", String.class, uname);
        this.strictUpdateFill(metaObject, "utcModify", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 是否应给当前实体填充 tenantId：
     * 跳过 eva.tenant.ignoreTables 中配置的系统级表
     */
    private boolean shouldFillTenantId(MetaObject metaObject) {
        Object obj = metaObject.getOriginalObject();
        if (obj == null) {
            return false;
        }
        TableName tableNameAnno = obj.getClass().getAnnotation(TableName.class);
        if (tableNameAnno == null) {
            return false;
        }
        String tableName = tableNameAnno.value();
        List<String> ignoreTables = evaConfig.getTenant().getIgnoreTables();
        if (ignoreTables == null || ignoreTables.isEmpty()) {
            return true;
        }
        return ignoreTables.stream().noneMatch(t -> t.equalsIgnoreCase(tableName));
    }

    /**
     * 不管是否有值 都填充
     */
    @Override
    public MetaObjectHandler strictFillStrategy(MetaObject metaObject, String fieldName, Supplier<?> fieldVal) {
        Object obj = fieldVal.get();
        if (Objects.nonNull(obj)) {
            metaObject.setValue(fieldName, obj);
        }
        return this;
    }
}
