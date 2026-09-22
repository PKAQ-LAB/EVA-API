package org.pkaq.core.log.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.pkaq.core.advice.ExceptionInfo;

import java.time.LocalDateTime;

/**
 * 可聚合、可处置的错误事件。
 *
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class ErrorIncident extends ExceptionInfo {
    /** 错误指纹 */
    private String fingerprint;
    /** 首次发生时间 */
    private LocalDateTime firstOccurredAt;
    /** 最后发生时间 */
    private LocalDateTime lastOccurredAt;
    /** 发生次数 */
    private long occurrenceCount;
    /** 处置状态 */
    private String status;
    /** 请求耗时(ms) */
    private String spendTime;
}
