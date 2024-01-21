package tech.yunyue.core.log.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginlogEntity extends LogEntity {
    /**
     * 操作人
     **/
    @Schema(description = "操作人")
    private String operatorName;
    /**
     * 操作人账号
     **/
    @Schema(description = "操作人账号")
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
     * 请求ip
     **/
    @Schema(description = "请求ip")
    private String ip;

    @Override
    public String toString() {
        return  "用户="+operatorName+
                ", 账号= "+operator +" :"+ operateType +"系统：[" +
                ", 时间 ='" + operateDatetime + '\'' +
                ", 设备类型 ='" + device + '\'' +
                ", 版本 ='" + version + '\'' +
                ']';
    }

}
