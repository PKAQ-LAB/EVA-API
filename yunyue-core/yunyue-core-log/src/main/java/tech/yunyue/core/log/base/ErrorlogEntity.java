package tech.yunyue.core.log.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 异常日志实体类
 */
@Data
@Accessors(chain = true)
public class ErrorlogEntity extends LogEntity{
    /**
     * 请求时间
     **/
    private String requestTime;
    /**
     * 请求ip
     **/
    private String ip;
    /**
     * 请求耗时
     **/
    private String spendTime;
    /**
     * 类名
     **/
    private String className;
    /**
     * 方法名
     **/
    private String method;
    /**
     * 方法参数
     **/
    private String params;
    /**
     * 异常描述
     **/
    private String exDesc;
    /**
     * 操作人
     **/
    private String loginUser;
    /**
     * 创建人ID
     */
    private String createId;
    /**
     * 创建人岗位ID
     */
    private String postId;
    /**
     * 创建人部门ID
     */
    private String orgId;
    /**
     * 租户id
     */
    private String tenantId;

    @Override
    public String toString() {
        return "ErrorlogEntity{" +
                "requestTime='" + requestTime + '\'' +
                ", ip='" + ip + '\'' +
                ", spendTime='" + spendTime + '\'' +
                ", className='" + className + '\'' +
                ", method='" + method + '\'' +
                ", params='" + params + '\'' +
                ", exDesc='" + exDesc + '\'' +
                ", loginUser='" + loginUser + '\'' +
                '}';
    }
}
