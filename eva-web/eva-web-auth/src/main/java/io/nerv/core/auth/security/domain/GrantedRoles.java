package io.nerv.core.auth.security.domain;

import lombok.Data;

/**
 */
@Data
public class GrantedRoles {
    private String name;

    private String code;

    private String dataPermissionType;

    private String dataPermissionDeptid;

}
