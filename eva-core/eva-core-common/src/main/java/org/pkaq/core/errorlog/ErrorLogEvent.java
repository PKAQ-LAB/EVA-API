package org.pkaq.core.errorlog;

import org.springframework.context.ApplicationEvent;

/**
 * 错误日志事件
 *
 * @author PKAQ
 */
public class ErrorLogEvent extends ApplicationEvent {

    public ErrorLogEvent(ErrorLogEntity entity) {
        super(entity);
    }
}
