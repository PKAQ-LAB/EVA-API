package io.nerv.config

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
import io.nerv.core.util.SecurityHelper
import lombok.extern.slf4j.Slf4j
import org.apache.ibatis.reflection.MetaObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * Mybatis Plus 自动填充策略实现类
 */
@Component
class MybatisMetaObjectHandler : MetaObjectHandler {
    var log = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private val securityHelper: SecurityHelper? = null
    override fun insertFill(metaObject: MetaObject) {
        log.debug("start insert fill ....")
        this.strictInsertFill(metaObject, "createBy", String::class.java, securityHelper!!.jwtUserId)
        this.strictInsertFill(metaObject, "modifyBy", String::class.java, securityHelper.jwtUserId)
        this.strictInsertFill(metaObject, "gmtCreate", LocalDateTime::class.java, LocalDateTime.now())
        this.strictInsertFill(metaObject, "gmtModify", LocalDateTime::class.java, LocalDateTime.now())
    }

    override fun updateFill(metaObject: MetaObject) {
        log.debug("start update fill ....")
        this.strictUpdateFill(metaObject, "gmtCreate", LocalDateTime::class.java, LocalDateTime.now())
        this.strictUpdateFill(metaObject, "gmtModify", LocalDateTime::class.java, LocalDateTime.now())
    }
}