package org.pkaq.core.log.events;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.advice.ExceptionInfo;
import org.pkaq.core.log.base.ErrorIncident;
import org.pkaq.core.log.base.ErrorIncidentRepository;
import org.pkaq.core.log.util.ErrorFingerprint;
import org.pkaq.core.log.util.LogSanitizer;
import org.pkaq.core.util.BeanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 错误事件聚合入库处理器。
 *
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class ErrorIncidentEventHandler {
    private static final String OPEN_STATUS = "OPEN";

    private final ErrorIncidentRepository errorIncidentRepository;

    /**
     * 将异常元数据转为脱敏后的错误事件摘要。
     *
     * @param info 异常元数据
     */
    @EventListener
    @Order(100)
    @Async("log_task")
    public void onExceptionEvent(ExceptionInfo info) {
        ErrorIncident incident = new ErrorIncident();
        BeanUtils.copyProperties(info, incident);
        LocalDateTime now = LocalDateTime.now();
        incident.setParams(LogSanitizer.sanitize(incident.getParams(), 4_000));
        incident.setSummary(LogSanitizer.sanitize(incident.getSummary(), 1_000));
        incident.setFingerprint(ErrorFingerprint.calculate(incident));
        incident.setFirstOccurredAt(now);
        incident.setLastOccurredAt(now);
        incident.setOccurrenceCount(1L);
        incident.setStatus(OPEN_STATUS);
        this.errorIncidentRepository.save(incident);
    }
}
