package org.pkaq.core.log.events;

import org.pkaq.core.log.base.BizLogEntity;
import org.springframework.context.ApplicationEvent;

/**
 * 日志事件基类
 *
 * @author PKAQ
 */
public class LogEvent<T extends BizLogEntity> extends ApplicationEvent {

    public LogEvent(T entity) {
        super(entity);
    }
}
