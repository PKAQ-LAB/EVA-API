package tech.yunyue.core.log.events;

import org.springframework.context.ApplicationEvent;
import tech.yunyue.core.log.base.ErrorlogEntity;

/**
 * 错误日志事件
 */
public class ErrorLogEvent extends ApplicationEvent {

    public ErrorLogEvent(ErrorlogEntity entity) {
        super(entity);
    }
}
