package tech.yunyue.core.log.events;

import org.springframework.context.ApplicationEvent;
import tech.yunyue.core.log.base.LogEntity;

public class LogEvent<T extends LogEntity> extends ApplicationEvent {

    public LogEvent(T entity) {
        super(entity);
    }
}
