package tech.yunyue.core.log.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 业务日志模型类
 *
 * @author: S.PKAQ
 */
@Data
@Accessors(chain = true)
public class BizLogEntity extends LogEntity{
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
     * 操作描述
     **/
    @Schema(description = "操作描述")
    private String description;
    /**
     * 类名
     **/
    @Schema(description = "类名")
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
     * 设备类型
     **/
    @Schema(description = "设备类型")
    private String device;
    /**
     * 应用版本
     **/
    @Schema(description = "应用版本")
    private String version;
    /**
     * 创建人岗位ID
     */
    private String postId;
    /**
     * 创建人部门ID
     */
    private String orgId;
    /**
     * 创建人ID
     */
    private String createId;
    /**
     * 租户id
     */
    private String tenantId;

    @Override
    public String toString() {
        return  "用户操作了系统：[" +
                "操作用户 ='" + operator + '\'' +
                ", 操作类型 ='" + operateType + '\'' +
                ", 操作描述 ='" + description + '\'' +
                ", 操作时间 ='" + operateDatetime + '\'' +
                ", 设备类型 ='" + device + '\'' +
                ", 版本 ='" + version + '\'' +
                ']';
    }

}
