package tech.yunyue.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.threaduser.ThreadUserHelper;
import tech.yunyue.sys.role.service.RoleService;
import tech.yunyue.sys.user.service.UserService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取用户的角色和权限 用于鉴权 <br/>
 * [账号id->权限列表]的缓存模型简单，但是修改角色的权限后 缓存中所有拥有该角色的账号缓存信息都需要改变<br/>
 * 使用[账号id -> 角色id -> 权限列表] 的缓存模型 则只需要清除或修改 [角色id -> 权限列表] 一条缓存即可
 */
    @Component
    @RequiredArgsConstructor
    public class UserRolePermission implements StpInterface {
        private final RoleService roleService;
        private final UserService userService;
    /**
     * 资源权限 角色 - 资源路径 的map
     */
    private volatile Map<String, Collection<String>> rolePermMap = new ConcurrentHashMap<>();

    private final EvaConfig evaConfig;

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

        //从redis里面取全部角色和权限的集合
        SaTokenDao redisDao = StpUtil.getStpLogic().getSaTokenDao();
        Map<String, Collection<String>> redisRoles = (Map<String, Collection<String>>)redisDao.getObject(CommonConstant.REDIS_ROLES_PERMISSION_PREFIX_KEY);
        if (CollectionUtil.isNotEmpty(redisRoles)) {
            rolePermMap.putAll(redisRoles);
        }
        //redis没有就从数据库中取
        if (CollectionUtil.isEmpty(rolePermMap)) {
            synchronized (rolePermMap) {
                //双重检测
                if (CollectionUtil.isEmpty(rolePermMap)) {
                    loadResourceRoleUrlPermMap(this.roleService.listRoleNamesWithPath());
                    // 把角色和权限的集合保存起来 永不过期  todo 要不要改成一个角色一条缓存 这样子修改角色只需要修改一条记录 否则就是每次修改都删掉缓存重新放
                    redisDao.setObject(CommonConstant.REDIS_ROLES_PERMISSION_PREFIX_KEY,rolePermMap,SaTokenDao.NEVER_EXPIRE);
                }
            }
        }

        // 严格鉴权模式 仅允许访问授权资源 未授权资源一律禁止访问
        // 根据用户角色获取所有可访问资源路径
        List<String> permissionList = new ArrayList<>();
        roleList.forEach(item -> {
            if (null != rolePermMap.get(item)) {
                permissionList.addAll(rolePermMap.get(item));
            }
        });
        return permissionList;
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     * @param loginId  账号id
     * @param loginType 账号类型
     * @return
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        //从ThreadUserHelper取
        String[] roleArr= ThreadUserHelper.getUserRoles();
        if (roleArr != null) {
            return Arrays.stream(roleArr).toList();
        }
        //从redis中取 用户可能没有角色 所以只需要判断是否为null
        SaTokenDao dao = StpUtil.getStpLogic().getSaTokenDao();
        List<String> roles = (List<String>)dao.getObject(CommonConstant.REDIS_USER_ROLES_PREFIX_KEY+loginId);
        if (Objects.nonNull(roles)) return roles;

        // 查询数据库且将用户角色保存到redis中
        roles = userService.getRoleById((String)loginId);
        dao.setObject(CommonConstant.REDIS_USER_ROLES_PREFIX_KEY+loginId,roles,StpUtil.getTokenTimeout());
        return roles;
    }

    /**
     * 加载资源，初始化资源变量
     * 角色 - url+资源路径
     *
     */
    public void loadResourceRoleUrlPermMap(List<Map<String, String>> menusUrl) {
        menusUrl.stream().forEach(item -> {
            var role_code = item.get("code");
            rolePermMap.put(role_code, this.getValues(item));
        });
    }

    /**
     * 拼接资源url path+resource_url
     */
    private Collection<String> getValues(Map<String, String> item) {
        var role_code = item.get("code");
        var path = item.get("path");
        var resoure_path = item.get("resource_url");

        if (StrUtil.isNotBlank(resoure_path)) {
            resoure_path = resoure_path.startsWith("/") ? resoure_path.substring(1) : resoure_path;
        }
        path = path.endsWith("/") ? path + resoure_path : path + "/" + resoure_path;

        Collection<String> values = rolePermMap.get(role_code);
        if (null == values) {
            values = new ArrayList<>();
        }
        values.add(path);
        return values;
    }
}
