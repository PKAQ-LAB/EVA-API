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
 * @author PKAQ
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

    /**
     * 数据权限范围。
     **/
    private String dataScope;

    /**
     * 自定义数据权限组织 ID，多个 ID 使用英文逗号分隔。
     **/
    private String dataOrgIds;

    /**
     * setter 中一次性规范化：补 ROLE_ 前缀并大写
     * 替代原先在 getter 中改写字段的副作用写法（每次 get 都改字段，
     * 对 MyBatis 反射 / JSON 序列化都不友好）。
     */
    public void setCode(String code) {
        if (code == null || code.isEmpty()) {
            this.code = "";
            return;
        }
        String trimmed = code.trim();
        if (!trimmed.toUpperCase().startsWith(CommonConstant.AUTH_PREFIX)) {
            trimmed = CommonConstant.AUTH_PREFIX + trimmed;
        }
        this.code = trimmed.toUpperCase();
    }
}
