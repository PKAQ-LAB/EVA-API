package org.pkaq.core.threaduser;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.enums.BizCodeEnum;
import org.pkaq.core.exception.BizException;

import java.util.*;

/**
 * @author
 */
public class ThreadUserHelper {
    /**
     * 保存用户对象的ThreadLocal  在拦截器操作 添加、删除相关用户数据
     */
    private static final ThreadLocal<ThreadUser> userThreadLocal = new TransmittableThreadLocal<>();

    /**
     * 获取当前登录用户方法
     */
    public static ThreadUser getCurrentUser() {
        return userThreadLocal.get();
    }

    /**
     * 添加当前登录用户方法  在拦截器方法执行前调用设置获取用户
     *
     * @param user
     */
    public static void setCurrentUser(ThreadUser user) {
        userThreadLocal.set(user);
    }

    /**
     * 删除当前登录用户方法  在拦截器方法执行后 移除当前用户对象
     */
    public static void remove() {
        userThreadLocal.remove();
    }

    /**
     * 获取用户ID
     *
     * @return
     */
    public static String getUserId() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getUserId).orElse(null);
    }

    /**
     * 获取用户ID 获取不到抛出异常
     *
     * @return
     */
    public static String getUserIdEx() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getUserId).orElseThrow(() -> new BizException(BizCodeEnum.ACCOUNT_NOT_EXIST));
    }

    /**
     * 获取用户名
     *
     * @return
     */
    public static String getUserName() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getName).orElse(null);
    }

    /**
     * 获取用户名 获取不到抛出异常
     *
     * @return
     */
    public static String getUserNameEx() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getName).orElseThrow(() -> new BizException(BizCodeEnum.ACCOUNT_NOT_EXIST));
    }

    /**
     * 获取用户account
     */
    public static String getAccount() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getAccount).orElse(null);
    }

    /**
     * 获取用户手机号
     */
    public static String getTel() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getTel).orElse(null);
    }

    /**
     * 获取角色以及角色的数据权限类型
     *
     * @return
     */
    public static Map<String, ThreadUser.GrantedRoles> getUsetGrantedRoles() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getRolesMap).orElse(null);
    }

    /**
     * 获取角色code
     *
     * @return
     */
    public static String[] getUserRoles() {
        return Optional.ofNullable(getUsetGrantedRoles())
                .map(m -> m.values().stream().map(ThreadUser.GrantedRoles::getCode).toArray(String[]::new)).orElse(null);
    }

    /**
     * 获取角色 获取不到抛出异常
     *
     * @return
     */
    public static String[] getUserRolesEx() {
        return Optional.ofNullable(getUserRoles()).orElseThrow(() -> new BizException(BizCodeEnum.ACCOUNT_NOT_EXIST));
    }

    /**
     * 获取用户的数据权限类型
     */
    public static List<ThreadUser.GrantedRoles> getUsetGrantedRoleList() {
        return new ArrayList<>(Optional.ofNullable(getUsetGrantedRoles())
                .map(Map::values).orElse(Collections.emptyList()));
    }

    /**
     * 获取用户的角色id
     */
    public static List<String> getRoleIdsList() {
        return Optional.ofNullable(getUsetGrantedRoles())
                .map(m -> m.keySet().stream().toList()).orElse(null);
    }

    /**
     * 是否是管理员
     * 通过判断角色中是否包含管理员角色获得
     *
     * @return
     */
    public static boolean isAdmin() {
        var isAdmin = false;
        var roles = getUserRoles();
        if (null != roles) {
            isAdmin = ArrayUtil.contains(roles, CommonConstant.ADMIN_ROLE_NAME);
        }
        return isAdmin;
    }

    /**
     * 获取租户号
     *
     * @return
     */
    public static String getTenantId() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getTenantId).orElse(null);
    }

    /**
     * 获取租户code
     *
     * @return
     */
    public static String getTenantCode() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getTenantCode).orElse(null);
    }

    /**
     * 获取部门id
     */
    public static String getOrgId() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getDeptId).orElse("");
    }

    /**
     * 获取岗位id
     */
    public static String getPostId() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getPostId).orElse("");
    }

    /**
     * 当前用户操作的模块id
     *
     * @return
     */
    public static String getMid() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getModuleId).orElse("");
    }

    /**
     * 当前用户操作的模块code
     *
     * @return
     */
    public static String getMcode() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getModuleCode).orElse("");
    }

    /**
     * 得到当前用户角色和该角色拥有的所有权限
     * 当前路径是无需资源鉴权的路径||没打开资源鉴权时，该map为null（资源鉴权时才会设置角色和其权限）
     *
     * @return
     */
    public static Map<String, List<String>> getRolePermission() {
        return Optional.ofNullable(userThreadLocal.get()).map(ThreadUser::getRolePermissonMap).orElse(Collections.emptyMap());
    }

    /**
     * 当前用户的设备类型
     *
     * @return
     */
    public static String getDevice() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getDevice).orElse("");
    }

    /**
     * 当前用户的应用版本
     *
     * @return
     */
    public static String getVersion() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getVersion).orElse("");
    }

}
