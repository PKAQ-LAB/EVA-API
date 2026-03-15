package org.pkaq.core.advice;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 异常元数据，由 ExceptionAdvice 发布
 * 非日志概念，仅用于事件传递
 *
 * @author PKAQ
 */
@Data
@Accessors(chain = true)
public class ExceptionInfo {
    /** 请求时间 */
    private String requestTime;
    /** 请求IP（由 web 层补充） */
    private String ip;
    /** 异常类名 */
    private String className;
    /** 异常方法 */
    private String method;
    /** 请求参数（由 web 层补充） */
    private String params;
    /** 异常描述 */
    private String exDesc;
    /** 登录用户 */
    private String loginUser;
    /** 租户ID */
    private long tenantId;
}
