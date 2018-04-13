package org.pkaq.web.sys.role.entity;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.BaseEntity;

/**
 * 角色管理模型类
 * @author: S.PKAQ
 * @Datetime: 2018/4/13 7:26
 */
@Data
@Alias("role")
@TableName("sys_role")
@EqualsAndHashCode(callSuper = true)
public class RoleEntity extends BaseEntity {
    private String name;

    private String code;

    private String parentId;

    private String parentName;

    private String path;

    private String pathName;

    private Byte isleaf;

    private Integer orders;

    private Boolean locked;
}
