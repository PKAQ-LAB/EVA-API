package org.pkaq.core.auth.role.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

/**
 * 认证角色实体
 *
 * @author PKAQ
 */
@Data
@Alias("authRole")
@TableName("SYS_ROLE")
@EqualsAndHashCode(callSuper = true)
public class AuthRoleEntity extends StdEntity {

    /**
     * 编码
     **/
    private String code = "";

    /**
     * 名称
     **/
    private String name = "";

    /** 数据权限范围。 */
    private String dataScope;

    /** 逗号分隔的指定组织 ID。 */
    private String dataOrgIds;

    public String getCode() {
        if (!this.code.startsWith(CommonConstant.AUTH_PREFIX)) {
            this.code = CommonConstant.AUTH_PREFIX + this.code;
        }
        return this.code.toUpperCase();
    }
}
