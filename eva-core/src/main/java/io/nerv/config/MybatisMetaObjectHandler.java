package io.nerv.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.nerv.core.util.SecurityHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mybatis Plus 自动填充策略实现类
 */
@Slf4j
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    @Autowired
    private SecurityHelper securityHelper;

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("start insert fill ....");
        this.setInsertFieldValByName("createBy", securityHelper.getJwtUserId(), metaObject);
        this.setInsertFieldValByName("modifyBy", securityHelper.getJwtUserId(), metaObject);
        this.setInsertFieldValByName("gmtCreate", LocalDateTime.now(), metaObject);
        this.setInsertFieldValByName("gmtModify", LocalDateTime.now(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("start update fill ....");
        this.setUpdateFieldValByName("gmtModify", LocalDateTime.now(), metaObject);
        this.setUpdateFieldValByName("modifyBy", securityHelper.getJwtUserId(), metaObject);
    }
}
