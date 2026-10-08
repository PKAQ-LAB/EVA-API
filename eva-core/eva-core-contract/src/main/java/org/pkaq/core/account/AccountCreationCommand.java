package org.pkaq.core.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import org.pkaq.core.enums.FrozenEnumm;

import java.sql.Date;

/**
 * 账号创建参数，密码仅用于校验和单次哈希。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Data
public class AccountCreationCommand {
    private Long id;
    private String account;
    @JsonIgnore
    @ToString.Exclude
    private String password;
    private String avatar;
    private String nickName;
    private String tel;
    private String email;
    private FrozenEnumm frozen;
    private String remark;
    private Double sort;
    private String lastIp;
    private Date lastLogin;
}
