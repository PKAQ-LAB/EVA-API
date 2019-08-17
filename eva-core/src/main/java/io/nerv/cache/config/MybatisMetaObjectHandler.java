package io.nerv.cache.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Mybatis Plus 自动填充策略实现类
 */
@Slf4j
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("start insert fill ....");

        this.setInsertFieldValByName("gmtCreate", LocalDateTime.now(), metaObject);
        this.setInsertFieldValByName("gmtModify", LocalDateTime.now(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("start update fill ....");
        this.setInsertFieldValByName("gmtModify", LocalDateTime.now(), metaObject);
    }
}
