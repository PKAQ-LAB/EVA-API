package org.pkaq.sys.role.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 角色模块关系表
 *
 * @author PKAQ
 */
@Data
@Alias("roleModule")
@TableName("SYS_ROLERES_REF")
public class RoleResourceEntity {
    private Long roleId;

    private Long resourceId;
}
