package org.pkaq.sys.role.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

/**
 * 角色管理模型类
 *
 * @author: S.PKAQ
 */
@Data
@Alias("role")
@TableName("sys_role")
@EqualsAndHashCode(callSuper = true)
public class RoleEntity extends StdEntity {

    /**
     * 编码
     **/
    private String code = "";
    /**
     * 名称
     **/
    private String name = "";

    // 添加 ROLE_ 前缀 并转大写
    public String getCode() {

        if (!this.code.startsWith(CommonConstant.AUTH_PREFIX)) {
            this.code = CommonConstant.AUTH_PREFIX + this.code;
        }
        return this.code.toUpperCase();
    }
}
