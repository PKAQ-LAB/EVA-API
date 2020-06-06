package io.nerv.core.bizlog.supporter.mybatis

import cn.hutool.core.bean.BeanUtil
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import io.nerv.core.bizlog.base.BizLogEntity
import io.nerv.core.bizlog.base.BizLogSupporter
import io.nerv.core.bizlog.condition.MybatisSupporterCondition
import io.nerv.core.bizlog.supporter.mybatis.entity.MybatisBizLogEntity
import io.nerv.core.bizlog.supporter.mybatis.mapper.MybatisSupporterMapper
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component
import java.util.*

/**
 * mybatis 存储实现类
 * @author: S.PKAQ
 * @Datetime: 2018/9/27 8:38
 */
@Slf4j
@Component
@Conditional(MybatisSupporterCondition::class)
class MybatisSupporter : BizLogSupporter {
    private var bizLogEntity: BizLogEntity? = null

    @Autowired
    private val mybatisSupporterMapper: MybatisSupporterMapper? = null

    constructor() : super() {}
    constructor(bizLogEntity: BizLogEntity?) {
        this.bizLogEntity = bizLogEntity
    }

    override fun save(bizLogEntity: BizLogEntity?) {
        val mybatisBizLogEntity = MybatisBizLogEntity()
        BeanUtil.copyProperties(bizLogEntity, mybatisBizLogEntity)
        mybatisSupporterMapper!!.insert(mybatisBizLogEntity)
    }

    override val log: List<BizLogEntity?>?
        get() = mybatisSupporterMapper!!.selectList(null)

    override fun getLogByType(type: String?): List<BizLogEntity?>? {
        val mybatisBizLogEntity = MybatisBizLogEntity()
        mybatisBizLogEntity.operateType = type
        val wrapper = QueryWrapper(mybatisBizLogEntity)
        return mybatisSupporterMapper!!.selectList(wrapper)
    }

    override fun getLogAfter(dateTime: Date?): List<BizLogEntity?>? {
        val wrapper = QueryWrapper<MybatisBizLogEntity>()
        wrapper.ge("operate_datetime", dateTime)
        return mybatisSupporterMapper!!.selectList(wrapper)
    }

    override fun getLogBetween(begin: Date?, end: Date?): List<BizLogEntity?>? {
        val wrapper = QueryWrapper<MybatisBizLogEntity>()
        wrapper.between("operate_datetime", begin, end)
        return mybatisSupporterMapper!!.selectList(wrapper)
    }

    override fun cleanAll() {
        mybatisSupporterMapper!!.delete(null)
    }

    override fun cleanBefore(dateTime: Date?) {
        val wrapper = QueryWrapper<MybatisBizLogEntity>()
        wrapper.le("operate_datetime", dateTime)
        mybatisSupporterMapper!!.delete(wrapper)
    }

    override fun cleanBetween(begin: Date?, end: Date?) {
        val wrapper = QueryWrapper<MybatisBizLogEntity>()
        wrapper.between("operate_datetime", begin, end)
        mybatisSupporterMapper!!.delete(wrapper)
    }

    override fun print() {}
}