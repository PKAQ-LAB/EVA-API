package org.pkaq.core.auth.spi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import org.pkaq.core.enums.FrozenEnumm;
import java.util.Date;

/**
 * 认证使用的只读账号投影，与 ORM 实体及权限集合隔离。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Data
public class AccountSnapshot {
    private Long id;
    private String account;
    @JsonIgnore
    @ToString.Exclude
    private String password;
    private String name;
    private String nickName;
    private Long tenantId;
    private Long deptId;
    private Long permVer;
    private FrozenEnumm frozen;
    private FrozenEnumm tenantFrozen;
    private Date tenantExpirationDate;
}
