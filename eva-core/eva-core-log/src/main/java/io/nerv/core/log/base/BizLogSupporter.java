package io.nerv.core.log.base;

import java.util.Date;
import java.util.List;

/**
 * 业务日志持久化接口
 * @author: S.PKAQ
 */
public interface BizLogSupporter {
    /**
     * 保存日志
     */
    void save(BizLogEntity bizLogEntity);

    /**
     * 获取日志
     */
    List<? extends BizLogEntity> getLog();

    /**
     * 获取指定操作类型的日志
     * @param type 操作类型
     * @return 符合条件的结果集
     */
    List<? extends BizLogEntity> getLogByType(String type);
    /**
     * 获取某个时间之后的日志
     * @param dateTime 时间点
     * @return 符合条件的日志集合
     */
    List<? extends BizLogEntity> getLogAfter(Date dateTime);

    /**
     * 获取某个日期区间的日志
     * @param begin 开始日期区间
     * @param end   结束日期区间
     * @return 所查询区间的日志
     */
    List<? extends BizLogEntity> getLogBetween(Date begin, Date end);

    /**
     * 清除所有日志
     */
    void cleanAll();

    /**
     * 清除某个时间点之前的日志
     * @param dateTime 要清除的时间点
     */
    void cleanBefore(Date dateTime);

    /**
     * 清除某个时间区间的日志
     * @param begin 开始时间
     * @param end 结束时间
     */
    void cleanBetween(Date begin, Date end);

    /**
     * 打印当前操作日志
     */
    void print();
}
