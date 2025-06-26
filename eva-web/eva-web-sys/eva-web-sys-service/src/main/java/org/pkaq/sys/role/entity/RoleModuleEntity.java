package org.pkaq.sys.role.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 角色模块关系表
 *
 * @author: S.PKAQ
 */
@Data
@Alias("roleModule")
@TableName("SYS_ROLEMODULE_REF")
public class RoleModuleEntity {
    private Long roleId;

    private Long moduleId;

    private Long resourceId;
}
