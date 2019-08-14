package io.nerv.core.bizlog.base;

import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 业务日志模型类
 * @author: S.PKAQ
 * @Datetime: 2018/9/26 21:28
 */
@Data
@Accessors(chain = true)
public class BizLogEntity {
    /** 操作人 **/
    private String operator;
    /** 操作类型 **/
    private String operateType;
    /** 操作时间 **/
    private String operateDatetime;
    /** 操作描述 **/
    private String description;
    /** 类名 **/
    private String className;
    /** 方法名 **/
    private String method;
    /** 参数 **/
    private String params;
    /** 返回结果 **/
    private String response;
}
