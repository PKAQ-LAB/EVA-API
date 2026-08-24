package org.pkaq.sys.organization.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

import java.util.List;

/**
 * 组织/部门管理实体（树形）
 * <p>
 * 数据约定（与 sys_module / sys_post 一致）：
 * - pid 非空，根节点 pid = 0（DB NOT NULL DEFAULT 0）
 * - path 形如 "/{id}"（根）、"/{parentPath}/{id}"（子孙）
 * - isleaf 由 Service 自动维护
 *
 * @author PKAQ
 */
@Data
@Alias("organization")
@TableName("sys_organization")
@EqualsAndHashCode(callSuper = true)
public class OrganizationEntity extends StdEntity {

    @TableField(condition = SqlCondition.LIKE)
    private String name;

    @TableField(condition = SqlCondition.LIKE)
    private String code;

    /** 上级节点 ID，根节点 = 0 */
    private Long pid;

    /** 路径（id 链） */
    private String path;

    /** 是否叶子节点 */
    private Boolean isleaf;

    /** 上级节点名称，仅用于列表查询。 */
    @TableField(exist = false)
    private String parentName;

    /** 子节点（非数据库字段，仅在树形组装时使用） */
    @TableField(exist = false)
    private List<OrganizationEntity> children;
}
