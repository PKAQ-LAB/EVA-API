package org.pkaq.core.bizlog.base;

import lombok.Data;

/**
 * 业务日志模型类
 * @author: S.PKAQ
 * @Datetime: 2018/9/26 21:28
 */
@Data
public class BizLogEntity {
    /** 操作人 **/
    private String operator;
    /** 操作类型 **/
    private String operateType;
    /** 操作时间 **/
    private String operateDatetime;
    /** 操作描述 **/
    private String description;
}
