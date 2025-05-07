package org.pkaq.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Mybatis Plus 自动填充策略实现类
 * @author PKAQ
 */
@Slf4j
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {


    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("start insert fill ....");
        this.strictInsertFill(metaObject, "createId", String.class, ThreadUserHelper.getUserId());
        this.strictInsertFill(metaObject, "createBy", String.class, ThreadUserHelper.getUserName());
        this.strictInsertFill(metaObject, "utcCreate", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "sort", Integer.class, 0);
        this.strictInsertFill(metaObject, "frozen", String.class, "0000");
        this.strictInsertFill(metaObject, "deleted", String.class, "0");
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("start update fill ....");
        this.strictInsertFill(metaObject, "modifyId", String.class, ThreadUserHelper.getUserId());
        this.strictUpdateFill(metaObject, "modifyBy", String.class, ThreadUserHelper.getUserName());
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
