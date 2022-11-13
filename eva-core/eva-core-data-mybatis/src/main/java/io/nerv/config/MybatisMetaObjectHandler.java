package io.nerv.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.nerv.common.threaduser.ThreadUserHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mybatis Plus 自动填充策略实现类
 */
@Slf4j
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {


    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("start insert fill ....");
        this.strictInsertFill(metaObject,"createBy", String.class , ThreadUserHelper.getUserId());
        this.strictInsertFill(metaObject,"modifyBy", String.class ,ThreadUserHelper.getUserId());
        this.strictInsertFill(metaObject,"gmtCreate", LocalDateTime.class ,LocalDateTime.now());
        this.strictInsertFill(metaObject,"gmtModify", LocalDateTime.class ,LocalDateTime.now());

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("start update fill ....");
        this.strictUpdateFill(metaObject,"modifyBy", String.class ,ThreadUserHelper.getUserId());
        this.strictUpdateFill(metaObject,"gmtModify", LocalDateTime.class ,LocalDateTime.now());
    }
}
