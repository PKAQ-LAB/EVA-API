package org.pkaq.core.log.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.pkaq.core.advice.ExceptionInfo;

/**
 * 错误日志实体
 *
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class ErrorLogEntity extends ExceptionInfo {
    /** 请求耗时(ms) */
    private String spendTime;
}
