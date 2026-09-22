package org.pkaq.core.log.base;

import org.pkaq.core.log.bo.LogQueryBo;

/**
 * 业务日志存储接口。
 *
 * @author PKAQ
 */
public interface BusinessLogRepository {

    /**
     * 保存日志
     *
     * @param bizLogEntity 业务日志实体
     */
    void save(BizLogEntity bizLogEntity);

    /**
     * 根据id获取日志详情
     *
     * @param id 日志id
     * @return 日志详情JSON
     */
    String get(String id);

    /**
     * 获取日志列表(分页)
     *
     * @param dateRangeBo 日期范围查询参数
     * @return 分页结果
     */
    Object list(LogQueryBo queryBo);
}
