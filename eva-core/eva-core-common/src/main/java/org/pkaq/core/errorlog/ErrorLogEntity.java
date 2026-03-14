package org.pkaq.core.errorlog;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 错误日志基础实体
 *
 * @author PKAQ
 */
@Data
@Accessors(chain = true)
public class ErrorLogEntity {
    /** 请求时间 */
    private String requestTime;
    /** 请求IP */
    private String ip;
    /** 请求耗时(ms) */
    private String spendTime;
    /** 异常类名 */
    private String className;
    /** 异常方法 */
    private String method;
    /** 请求参数 */
    private String params;
    /** 异常描述 */
    private String exDesc;
    /** 登录用户 */
    private String loginUser;
    /** 租户ID */
    private long tenantId;
}
