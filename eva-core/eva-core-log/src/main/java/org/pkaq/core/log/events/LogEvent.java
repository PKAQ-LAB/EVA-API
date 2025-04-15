package org.pkaq.core.log.events;

import org.pkaq.core.log.base.LogEntity;
import org.springframework.context.ApplicationEvent;

public class LogEvent<T extends LogEntity> extends ApplicationEvent {

    public LogEvent(T entity) {
        super(entity);
    }
}
