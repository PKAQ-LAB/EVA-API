package org.pkaq.sys.post.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

/**
 * 角色用户关系表
 *
 * @author: S.PKAQ
 */
@Data
@Alias("postUser")
@TableName("SYS_POSTUSER_REF")
@EqualsAndHashCode()
public class PostUserEntity {
    @NotBlank
    private Long postId;

    @NotBlank
    private Long userId;
}
