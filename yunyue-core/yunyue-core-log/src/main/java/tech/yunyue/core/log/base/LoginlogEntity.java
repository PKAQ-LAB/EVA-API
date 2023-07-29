package tech.yunyue.core.log.base;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginlogEntity extends LogEntity{
    /**
     * 操作人
     **/
    private String operatorName;
    /**
     * 操作人账号
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
     * 设备类型
     **/
    private String device;
    /**
     * 应用版本
     **/
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
        return  "用户="+operatorName+
                ", 账号= "+operator +" :"+ operateType +"系统：[" +
                ", 时间 ='" + operateDatetime + '\'' +
                ", 设备类型 ='" + device + '\'' +
                ", 版本 ='" + version + '\'' +
                ']';
    }

}
