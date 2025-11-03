package org.pkaq.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.pkaq.core.enums.DelEnumm;

import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Mybatis Plus 自动填充策略实现类
 * @author PKAQ
 */
@Slf4j
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {
    private final String ANONYMOUS = "Anonymous";

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
