package tech.yunyue.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import tech.yunyue.auth.domain.JwtUserFactory;
import tech.yunyue.auth.service.JDBCService;
import tech.yunyue.core.cache.util.RedisUtil;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.threaduser.ThreadUser;
import tech.yunyue.core.threaduser.ThreadUserHelper;
import tech.yunyue.core.util.json.JsonUtil;

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
    private final RedisUtil redisUtil;

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
        roleList.forEach(roleId -> {
            String key = CommonConstant.REDIS_ROLES_PERMISSION_PREFIX_KEY+roleId;
            String redisStr = redisUtil.get(key);
            if (StringUtils.isEmpty(redisStr)) {
                synchronized (key.intern()) {
                    redisStr = redisUtil.get(key);
                    if(StringUtils.isEmpty(redisStr)){
                        //通过角色id查询查数据库
                        redisStr = JsonUtil.toJson(this.jdbcService.listRoleNamesWithPath(roleId));
                        redisUtil.set(key, redisStr);
                    }
                }
            }
            permissionList.addAll(JsonUtil.parseArray(redisStr, String.class));
        });
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
        //从redis中取 用户可能没有角色 所以只需要判断是否为null
        SaTokenDao dao = StpUtil.getStpLogic().getSaTokenDao();
        Map<String, ThreadUser.GrantedRoles> roles = (Map<String, ThreadUser.GrantedRoles>)dao.getObject(CommonConstant.REDIS_USER_ROLES_PREFIX_KEY+loginId);
        if (Objects.isNull(roles)){
            // 查询数据库且将用户角色保存到redis中
            roles = JwtUserFactory.mapToGrantedAuthorities(this.jdbcService.getRoleById((String)loginId));
            dao.setObject(CommonConstant.REDIS_USER_ROLES_PREFIX_KEY+loginId, roles, StpUtil.getTokenTimeout());
        }
        return roles.keySet().stream().toList();
    }
}
