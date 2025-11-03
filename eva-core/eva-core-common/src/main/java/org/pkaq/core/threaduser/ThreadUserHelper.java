package org.pkaq.core.threaduser;

import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.constant.CommonConstant;

import java.util.*;
import java.util.function.Function;

/**
 * 用户上下文工具类，用于在当前线程中获取用户信息
 *
 * @author PKAQ
 */
@Slf4j
public class ThreadUserHelper {
    /**
     * 存储用户对象的 ThreadLocal，支持线程传递
     */
    private static final ScopedValue<ThreadUser> userThreadLocal = ScopedValue.newInstance();

    /**
     * 获取当前登录用户
     */
    public static ThreadUser getCurrentUser() {
        return userThreadLocal.get();
    }

    /**
     * 设置当前登录用户
     *
     * @param user 用户对象
     */
    public static void setCurrentUser(ThreadUser user) {
        ScopedValue.where(userThreadLocal, user);
    }

    /**
     * 替代 set + remove：
     * 在作用域中设置用户信息并执行逻辑，退出作用域自动清理
     */
    public static void runWithUser(ThreadUser user, Runnable runnable) {
        ScopedValue.where(userThreadLocal, user).run(runnable);
    }

    // ====================== 工具方法 ======================

    /**
     * 安全获取用户字段，并记录未找到用户日志
     */
    private static <T> T safeGet(Function<ThreadUser, T> extractor, String fieldName) {
        if (userThreadLocal.isBound()) {
            return extractor.apply(userThreadLocal.get());
        } else {
            log.warn("获取用户【{}】失败：当前线程中没有用户信息", fieldName);
            return null;
        }
    }

    private static Long safeGetLong(Function<ThreadUser, Long> extractor, String fieldName) {
        if (userThreadLocal.isBound()) {
            return extractor.apply(userThreadLocal.get());
        } else {
            log.warn("获取用户【{}】失败：当前线程中没有用户信息", fieldName);
            return -1L;
        }
    }

    /**
     * 安全获取字符串类型字段，默认返回空字符串
     */
    private static String safeGetString(Function<ThreadUser, String> extractor) {
        if (userThreadLocal.isBound()) {
            return extractor.apply(userThreadLocal.get());
        } else {
            log.warn("获取用户失败：当前线程中没有用户信息");
            return "";
        }
    }

    /**
     * 安全获取字符串数组字段，默认返回空数组
     */
    private static String[] safeGetStringArray(Function<ThreadUser, String[]> extractor) {
        if (userThreadLocal.isBound()) {
            return extractor.apply(userThreadLocal.get());
        } else {
            log.warn("获取用户失败：当前线程中没有用户信息");
            return new String[0];
        }
    }

    /**
     * 安全获取 Map<String, GrantedRoles>
     */
    private static Map<Long, ThreadUser.GrantedRoles> safeGetRolesMap() {
        if (userThreadLocal.isBound()) {
            return userThreadLocal.get().getRolesMap();
        } else {
            log.warn("获取用户角色失败：当前线程中没有用户信息");
            return Collections.emptyMap();
        }
    }

    // ====================== 用户基础信息 ======================

    /**
     * 获取用户ID，找不到返回 null
     */
    public static long getUserId() {
        return safeGetLong(ThreadUser::getUserId, "用户ID");
    }

    /**
     * 获取用户名，找不到返回 null
     */
    public static String getUserName() {
        return safeGet(ThreadUser::getName, "用户名");
    }

    /**
     * 获取账号
     */
    public static String getAccount() {
        return safeGet(ThreadUser::getAccount, "账号");
    }

    /**
     * 获取手机号
     */
    public static String getTel() {
        return safeGet(ThreadUser::getTel, "手机号");
    }

    /**
     * 获取租户ID
     */
    public static long getTenantId() {
        return safeGetLong(ThreadUser::getTenantId, "租户id");
    }

    /**
     * 获取租户编码
     */
    public static String getTenantCode() {
        return safeGetString(ThreadUser::getTenantCode);
    }

    /**
     * 获取部门ID
     */
    public static long getOrgId() {
        return safeGetLong(ThreadUser::getDeptId, "部门id");
    }

    /**
     * 获取岗位ID
     */
    public static long getPostId() {
        return safeGetLong(ThreadUser::getPostId, "岗位id");
    }

    // ====================== 角色与权限相关 ======================

    /**
     * 获取用户角色编码数组，找不到返回空数组
     */
    public static String[] getUserRoles() {
        return safeGetStringArray(user -> {
            Map<Long, ThreadUser.GrantedRoles> rolesMap = user.getRolesMap();
            if (rolesMap == null || rolesMap.isEmpty()) {
                return new String[0];
            }
            return rolesMap.values().stream()
                    .map(ThreadUser.GrantedRoles::getCode)
                    .toArray(String[]::new);
        });
    }

    /**
     * 获取用户角色 Map
     */
    public static Map<Long, ThreadUser.GrantedRoles> getUsetGrantedRoles() {
        return safeGetRolesMap();
    }

    /**
     * 获取用户角色列表
     */
    public static List<ThreadUser.GrantedRoles> getUsetGrantedRoleList() {
        return new ArrayList<>(getUsetGrantedRoles().values());
    }

    /**
     * 获取角色ID列表
     */
    public static List<Long> getRoleIdsList() {
        return Optional.ofNullable(getUsetGrantedRoles())
                .map(Map::keySet)
                .map(List::copyOf)
                .orElse(Collections.emptyList());
    }

    /**
     * 判断是否是管理员
     */
    public static boolean isAdmin() {
        String[] roles = getUserRoles();
        return roles != null && ArrayUtil.contains(roles, CommonConstant.ADMIN_ROLE_NAME);
    }

    // ====================== 模块信息 ======================

    /**
     * 当前用户操作的模块ID
     */
    public static long getMid() {
        return safeGet(ThreadUser::getModuleId, "模块id");
    }

    /**
     * 当前用户操作的模块编码
     */
    public static String getMcode() {
        return safeGetString(ThreadUser::getModuleCode);
    }

    /**
     * 获取角色权限映射表
     */
    public static Map<Long, List<Long>> getRolePermission() {
        if (userThreadLocal.isBound()) {
            return userThreadLocal.get().getRolePermissonMap();
        } else {
            log.warn("获取用户失败：当前线程中没有用户信息");
            return Collections.emptyMap();
        }
    }

    // ====================== 设备 & 版本 ======================

    /**
     * 当前用户的设备类型
     */
    public static String getDevice() {
        return safeGetString(ThreadUser::getDevice);
    }

    /**
     * 当前用户的应用版本
     */
    public static String getVersion() {
        return safeGetString(ThreadUser::getVersion);
    }
}