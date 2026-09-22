package org.pkaq.core.log.base;

import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mvc.vo.PageVo;

/**
 * 错误事件存储接口。
 *
 * @author PKAQ
 */
public interface ErrorIncidentRepository {

    /**
     * 保存错误日志
     *
     * @param entity 错误日志实体
     */
    void save(ErrorIncident entity);

    /**
     * 根据ID获取错误日志详情
     *
     * @param id 日志ID
     * @return 错误日志实体
     */
    ErrorIncident get(String id);

    /**
     * 分页查询错误日志列表
     *
     * @param dateRangeBo 日期范围
     * @param pageNo      页码
     * @param pageSize    每页条数
     * @return 分页结果
     */
    PageVo<? extends ErrorIncident> list(DateRangeBo dateRangeBo, int pageNo, int pageSize);
}
