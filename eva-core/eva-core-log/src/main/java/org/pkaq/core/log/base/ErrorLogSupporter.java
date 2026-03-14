package org.pkaq.core.log.base;

import org.pkaq.core.errorlog.ErrorLogEntity;
import org.pkaq.core.errorlog.ErrorLogEvent;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mvc.vo.PageVo;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * 错误日志持久化接口
 *
 * @author PKAQ
 */
public interface ErrorLogSupporter {

    /**
     * 保存错误日志
     *
     * @param entity 错误日志实体
     */
    void save(ErrorLogEntity entity);

    /**
     * 根据ID获取错误日志详情
     *
     * @param id 日志ID
     * @return 错误日志实体
     */
    ErrorLogEntity get(String id);

    /**
     * 分页查询错误日志列表
     *
     * @param dateRangeBo 日期范围
     * @param pageNo      页码
     * @param pageSize    每页条数
     * @return 分页结果
     */
    PageVo<? extends ErrorLogEntity> list(DateRangeBo dateRangeBo, int pageNo, int pageSize);

    /**
     * 监听错误日志事件，异步保存
     *
     * @param event 错误日志事件
     */
    @EventListener
    @Async("log_task")
    default void onErrorLogEvent(ErrorLogEvent event) {
        this.save((ErrorLogEntity) event.getSource());
    }
}
