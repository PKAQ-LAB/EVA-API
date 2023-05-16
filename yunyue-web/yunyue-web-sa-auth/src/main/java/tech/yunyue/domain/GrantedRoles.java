package tech.yunyue.domain;

import lombok.Data;

/**
 * 用户的角色
 */
@Data
public class GrantedRoles {
    private String name;

    private String code;

    private String dataPermissionType;

    private String dataPermissionDeptid;

}
