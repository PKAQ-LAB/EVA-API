package io.nerv.core.bizlog.base

import java.util.*

/**
 * 业务日志持久化接口
 * @author: S.PKAQ
 * @Datetime: 2018/9/26 21:22
 */
interface BizLogSupporter {
    /**
     * 保存日志
     */
    fun save(bizLogEntity: BizLogEntity?)

    /**
     * 获取日志
     */
    val log: List<BizLogEntity?>?

    /**
     * 获取指定操作类型的日志
     * @param type 操作类型
     * @return 符合条件的结果集
     */
    fun getLogByType(type: String?): List<BizLogEntity?>?

    /**
     * 获取某个时间之后的日志
     * @param dateTime 时间点
     * @return 符合条件的日志集合
     */
    fun getLogAfter(dateTime: Date?): List<BizLogEntity?>?

    /**
     * 获取某个日期区间的日志
     * @param begin 开始日期区间
     * @param end   结束日期区间
     * @return 所查询区间的日志
     */
    fun getLogBetween(begin: Date?, end: Date?): List<BizLogEntity?>?

    /**
     * 清除所有日志
     */
    fun cleanAll()

    /**
     * 清除某个时间点之前的日志
     * @param dateTime 要清除的时间点
     */
    fun cleanBefore(dateTime: Date?)

    /**
     * 清除某个时间区间的日志
     * @param begin 开始时间
     * @param end 结束时间
     */
    fun cleanBetween(begin: Date?, end: Date?)

    /**
     * 打印当前操作日志
     */
    fun print()
}