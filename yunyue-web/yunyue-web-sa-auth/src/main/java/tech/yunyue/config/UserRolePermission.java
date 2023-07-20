package tech.yunyue.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.yunyue.auth.service.JDBCService;
import tech.yunyue.core.threaduser.ThreadUser;
import tech.yunyue.core.threaduser.ThreadUserHelper;

import java.util.*;

/**
 * 获取用户的角色和权限 用于鉴权 <br/>
 * [账号id->权限列表]的缓存模型简单，但是修改角色的权限后 缓存中所有拥有该角色的账号缓存信息都需要改变<br/>
 * 使用[账号id -> 角色id -> 权限列表] 的缓存模型 则只需要清除或修改 [角色id -> 权限列表] 一条缓存即可
 */
@Component
@RequiredArgsConstructor
public class UserRolePermission implements StpInterface {
    private final JDBCService jdbcService;
    /**
     * 返回一个账号所拥有的权限码集合
     * @param loginId  账号id
     * @param loginType 账号类型
     * @return
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        //得到用户角色
        List<String> roleList = getRoleList(loginId,loginType);
        if (CollectionUtil.isEmpty(roleList)) return Collections.emptyList();
        // 根据用户角色获取所有可访问资源路径
        Set<String> permissionList = new HashSet<>();
        roleList.forEach(roleId -> permissionList.addAll(this.jdbcService.listRoleNamesWithPath(roleId)));
        return new ArrayList<>(permissionList);
    }

    /**
     * 返回一个账号所拥有的角色id集合
     * @param loginId  账号id
     * @param loginType 账号类型
     * @return
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        //从ThreadUserHelper取
        List<String> roleList= ThreadUserHelper.getRoleIdsList();
        if (roleList != null) {
            return roleList;
        }
        Map<String, ThreadUser.GrantedRoles> roles = this.jdbcService.getRoleById((String)loginId);
        return roles.keySet().stream().toList();
    }
}
