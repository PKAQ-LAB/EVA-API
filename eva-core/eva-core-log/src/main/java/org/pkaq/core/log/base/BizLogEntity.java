package org.pkaq.core.log.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 业务日志模型类
 *
 * @author PKAQ
 */
@Data
@Accessors(chain = true)
public class BizLogEntity {
    /**
     * 操作人
     **/
    @Schema(description = "操作人")
    private String operator;
    /**
     * 操作类型
     **/
    @Schema(description = "操作类型")
    private String operateType;
    /**
     * 操作时间
     **/
    @Schema(description = "操作时间")
    private String operateDatetime;
    /**
     * 请求耗时
     **/
    @Schema(description = "请求耗时")
    private String spendTime;
    /**
     * 操作描述
     **/
    @Schema(description = "操作描述")
    private String description;
    /**
     * 模块code
     **/
    @Schema(description = "模块code")
    private String mCode;
    /**
     * 业务id
     **/
    @Schema(description = "业务id")
    private String bId;
    /**
     * 绫诲悕
     **/
    @Schema(description = "绫诲悕")
    private String className;
    /**
     * 方法名
     **/
    @Schema(description = "方法名")
    private String method;

    /**
     * 参数
     **/
    @Schema(description = "参数")
    private String params;
    /**
     * 返回结果
     **/
    @Schema(description = "返回结果")
    private String response;
    /**
     * 璁惧绫诲瀷
     **/
    @Schema(description = "璁惧绫诲瀷")
    private String device;
    /**
     * 搴旂敤鐗堟湰
     **/
    @Schema(description = "搴旂敤鐗堟湰")
    private String version;

    @Override
    public String toString() {
        return "用户操作了系统：[" +
                "操作用户 ='" + operator + '\'' +
                ", 操作类型 ='" + operateType + '\'' +
                ", 操作描述 ='" + description + '\'' +
                ", 操作时间 ='" + operateDatetime + '\'' +
                ", 璁惧绫诲瀷 ='" + device + '\'' +
                ", 鐗堟湰 ='" + version + '\'' +
                ']';
    }

}
