package tech.yunyue.core.event;

import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 踢出用户事件
 */
public class KickUserEvent extends ApplicationEvent {
    public KickUserEvent(List<String> idList) {
        super(idList);
    }
}
