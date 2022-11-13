package io.nerv.common.bizlog.events;

import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * 系统日志事件
 *
 * @author PKAQ
 */
public class BizLogEvent extends ApplicationEvent {

    public BizLogEvent(Map<String, Object> source) {
        super(source);
    }
}