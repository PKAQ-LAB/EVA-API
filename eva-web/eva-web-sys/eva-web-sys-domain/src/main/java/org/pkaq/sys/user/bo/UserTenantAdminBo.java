package org.pkaq.sys.user.bo;

import lombok.Data;
import org.pkaq.core.mvc.bo.Bo;

/**
 * 租户管理员创建参数。
 *
 * @author PKAQ
 */
@Data
public class UserTenantAdminBo implements Bo {
    private Long id;
    private String account;
    private String password;
    private Long tenantId;
}
