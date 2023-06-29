package tech.yunyue.core.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.ApplicationEvent;
import tech.yunyue.core.enums.BizCode;
import tech.yunyue.core.enums.BizCodeEnum;

import java.util.List;

/**
 * 自定义事件
 */
public class BizEvent extends ApplicationEvent {
    public BizEvent(String eventName, Object eventObj) {
        super(new Event(eventName, eventObj));
    }

    @Data
    @AllArgsConstructor
    public static  class Event {
        private String eventName;
        private Object obj;
    }
}
