package io.nerv.core.bizlog.supporter.console

import io.nerv.core.bizlog.base.BizLogEntity
import io.nerv.core.bizlog.base.BizLogSupporter
import io.nerv.core.bizlog.condition.DefaultSupporterCondition
import lombok.extern.slf4j.Slf4j
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component
import java.util.*

/**
 * 控制台日志实现类
 * @author: S.PKAQ
 * @Datetime: 2018/9/26 21:48
 */
@Slf4j
@Component
@Conditional(DefaultSupporterCondition::class)
class ConsoleSupporter : BizLogSupporter {
    private var bizLogEntity: BizLogEntity? = null

    constructor() : super() {}
    constructor(bizLogEntity: BizLogEntity?) {
        this.bizLogEntity = bizLogEntity
    }

    override fun save(bizLogEntity: BizLogEntity?) {}
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
    override fun print() {
        ConsoleSupporter.log.info("Biz log: " + bizLogEntity.getDescription())
    }
}