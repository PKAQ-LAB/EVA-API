package tech.yunyue.core.log.base;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Field;

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
    @Field("operate_type")
    private String operateType;
    /**
     * 操作时间
     **/
    @Field("operate_datetime")
    private String operateDatetime;
    /**
     * 操作描述
     **/
    private String description;
    /**
     * 类名
     **/
    @Field("class_name")
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
    /**
     * 创建人岗位ID
     */
    @Field("post_id")
    private String postId;
    /**
     * 创建人部门ID
     */
    @Field("org_id")
    private String orgId;
    /**
     * 创建人ID
     */
    @Field("create_id")
    private String createId;
    /**
     * 租户id
     */
    @Field("tenant_id")
    private String tenantId;

    @Override
    public String toString() {
        return  "用户登录了系统：[" +
                "登录用户 ='" + operator + '\'' +
                ", 登录时间 ='" + operateDatetime + '\'' +
                ", 设备类型 ='" + device + '\'' +
                ", 版本 ='" + version + '\'' +
                ']';
    }

}
