package tech.yunyue.core.threaduser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * 用于trheadlocal存储用户信息的实体类
 */
@Data
@Accessors(chain = true)
public class ThreadUser implements Serializable {
    // 用户id
    private String userId;
    // 用户账号
    private String account;
    // 用户手机号
    private String tel;
    // 用户名称
    private String name;
    // 用户拥有的角色以及角色的数据权限类型
    private Map<String, GrantedRoles> rolesMap;
    // 用户拥有的数据权限
    private String dataPermission;
    // 用户的租户id
    private String tenantId;
    // 当前用户操作的模块id
    private String moduleId;
    // 当前用户操作的模块code
    private String moduleCode;
    // 当前用户部门id
    private String deptId;
    // 当前用户岗位id
    private String postId;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GrantedRoles {
        private String name;

        private String code;

        private String dataPermissionType;

        private String dataPermissionDeptid;
    }
}

