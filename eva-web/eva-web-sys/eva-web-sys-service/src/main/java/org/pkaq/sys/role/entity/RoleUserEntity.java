package org.pkaq.sys.role.entity;

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
@Alias("roleUser")
@TableName("SYS_ROLEUSER_REF")
@EqualsAndHashCode()
public class RoleUserEntity {
    @NotBlank
    private Long roleId;

    @NotBlank
    private Long userId;
}
