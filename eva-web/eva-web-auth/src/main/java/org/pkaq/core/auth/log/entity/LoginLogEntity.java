package org.pkaq.core.auth.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * 登录日志实体
 *
 * @author PKAQ
 */
@Data
@Alias("loginLog")
@TableName("SYS_LOGIN_LOG")
public class LoginLogEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private Long userId;

    private String account;

    private String loginType;

    private Boolean success;

    private String failReason;

    private String ip;

    private String userAgent;

    private String device;

    private String version;

    private LocalDateTime utcCreate;
}
