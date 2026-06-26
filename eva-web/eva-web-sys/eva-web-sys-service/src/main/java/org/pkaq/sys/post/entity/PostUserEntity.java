package org.pkaq.sys.post.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.apache.ibatis.type.JdbcType;
import org.pkaq.core.mvc.entity.Entity;

import java.time.LocalDateTime;

/**
 * 岗位-用户 关系表
 * <p>
 * 字段约定：
 * - id：独立主键（避免 (postId, userId) 复合主键带来的 Mapper 操作复杂度）
 * - tenantId：多租户隔离字段
 * - 审计字段：记录授权人 / 授权时间（便于追溯）
 * - DB 层需要补 UNIQUE(post_id, user_id) 约束防止重复授权
 *
 * @author PKAQ
 */
@Data
@Alias("postUser")
@TableName("SYS_POSTUSER_REF")
@EqualsAndHashCode(callSuper = false)
public class PostUserEntity implements Entity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @NotNull
    private Long postId;

    @NotNull
    private Long userId;

    /** 租户ID */
    private Long tenantId;

    /** 授权人 ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createId;

    /** 授权人 */
    @TableField(fill = FieldFill.INSERT, jdbcType = JdbcType.VARCHAR)
    private String createBy;

    /** 授权时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime utcCreate;
}
