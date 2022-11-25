package io.nerv.core.log.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 业务日志模型类
 *
 * @author: S.PKAQ
 */
@Data
@Accessors(chain = true)
public class BizLogEntity {
    /**
     * 操作人
     **/
    private String operator;
    /**
     * 操作类型
     **/
    private String operateType;
    /**
     * 操作时间
     **/
    private String operateDatetime;
    /**
     * 操作描述
     **/
    private String description;
    /**
     * 类名
     **/
    private String className;
    /**
     * 方法名
     **/
    private String method;
    /**
     * 参数
     **/
    private String params;
    /**
     * 返回结果
     **/
    private String response;
    /**
     * 设备类型
     **/
    private String device;
    /**
     * 应用版本
     **/
    private String version;
}
