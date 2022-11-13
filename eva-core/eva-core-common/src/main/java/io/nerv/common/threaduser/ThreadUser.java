package io.nerv.common.threaduser;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 用于trheadlocal存储用户信息的实体类
 */
@Data
@Accessors(chain = true)
public class ThreadUser implements Serializable {
    // 用户id
    private String userId;
    // 用户名称
    private String userName;
    // 用户拥有的角色
    private String[] roles;
    // 用户拥有的数据权限
    private String dataPermission;
}
