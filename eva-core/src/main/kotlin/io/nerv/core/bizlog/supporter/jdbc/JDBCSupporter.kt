package io.nerv.core.bizlog.supporter.jdbc

import io.nerv.core.bizlog.base.BizLogEntity
import io.nerv.core.bizlog.base.BizLogSupporter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*

/**
 * 基于数据库的日志持久话类
 * @author: S.PKAQ
 * @Datetime: 2018/9/26 22:09
 */
class JDBCSupporter(private val bizLogEntity: BizLogEntity) : BizLogSupporter {

    @Autowired
    private val jdbcTemplate: JdbcTemplate? = null
    override fun save(entity: BizLogEntity?) {}
    override val log: List<BizLogEntity?>?
        get() = null

    override fun getLogByType(type: String?): List<BizLogEntity?>? {
        return null
    }

    override fun getLogAfter(dateTime: Date?): List<BizLogEntity?>? {
        return null
    }

    override fun getLogBetween(begin: Date?, end: Date?): List<BizLogEntity?>? {
        return null
    }

    override fun cleanAll() {}
    override fun cleanBefore(dateTime: Date?) {}
    override fun cleanBetween(begin: Date?, end: Date?) {}
    override fun print() {}

}