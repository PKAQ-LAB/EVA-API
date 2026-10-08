package org.pkaq.sys.user.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.Entity;

/**
 * 管理端可选档案，共享账号标识，不重复凭据和审计字段。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Data
@Alias("accountProfile")
@TableName("SYS_ACCOUNT_PROFILE")
public class AccountProfileEntity implements Entity {
    @TableId(value = "ACCOUNT_ID", type = IdType.INPUT)
    private Long accountId;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String code;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String name;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long deptId;

    /** 档案与账号共享同一主键。 */
    @Override
    public Long getId() {
        return accountId;
    }
}
