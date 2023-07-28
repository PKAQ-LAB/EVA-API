package tech.yunyue.core.log.events;

import tech.yunyue.core.log.base.ErrorlogEntity;

/**
 * 错误日志事件
 */
public class ErrorLogEvent extends LogEvent<ErrorlogEntity> {

    public ErrorLogEvent(ErrorlogEntity entity) {
        super(entity);
    }
}
