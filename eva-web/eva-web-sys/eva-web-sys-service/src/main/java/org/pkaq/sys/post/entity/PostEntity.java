package org.pkaq.sys.post.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

import java.util.List;

/**
 * 岗位信息（树形）
 * <p>
 * 数据约定（与 sys_module 范本一致）：
 * - pid 非空，根节点 pid = 0（DB NOT NULL DEFAULT 0）
 * - path 形如 "/{id}"（根）、"/{parentPath}/{id}"（子孙）
 * - isleaf 由 Service 自动维护，新增子节点时父置 false，删/移走最后一个子时父置 true
 *
 * @author AOC
 */
@Data
@Alias("post")
@TableName("SYS_POST")
@EqualsAndHashCode(callSuper = true)
public class PostEntity extends StdEntity {
    /** 编码 */
    private String code;

    /** 岗位名称 */
    private String title;

    /** 职级 */
    private String level;

    /** 上级岗位 ID，根节点 = 0 */
    private Long pid;

    /** 路径（id 链） */
    private String path;

    /** 是否叶子节点 */
    private Boolean isleaf;

    /** 子节点（非数据库字段，仅在树形组装时使用） */
    @TableField(exist = false)
    private List<PostEntity> children;
}
