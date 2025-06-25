package org.pkaq.core.threaduser;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.constant.CommonConstant;

import java.util.*;
import java.util.function.Function;

/**
 * 用户上下文工具类，用于在当前线程中获取用户信息
 * @author PKAQ
 */
@Slf4j
public class ThreadUserHelper {
    /**
     * 存储用户对象的 ThreadLocal，支持线程传递
     */
    private static final ThreadLocal<ThreadUser> userThreadLocal = new TransmittableThreadLocal<>();

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
        userThreadLocal.set(user);
    }

    /**
     * 移除当前用户对象
     */
    public static void remove() {
        userThreadLocal.remove();
    }

    // ====================== 工具方法 ======================

    /**
     * 安全获取用户字段，并记录未找到用户日志
     */
    private static <T> T safeGet(Function<ThreadUser, T> extractor, String fieldName) {
        return Optional.ofNullable(userThreadLocal.get())
                .map(extractor)
                .orElseGet(() -> {
                    log.warn("获取用户【{}】失败：当前线程中没有用户信息", fieldName);
                    return null;
                });
    }

    /**
     * 安全获取字符串类型字段，默认返回空字符串
     */
    private static String safeGetString(Function<ThreadUser, String> extractor) {
        return Optional.ofNullable(userThreadLocal.get())
                .map(extractor)
                .orElse("");
    }

    /**
     * 安全获取字符串数组字段，默认返回空数组
     */
    private static String[] safeGetStringArray(Function<ThreadUser, String[]> extractor) {
        return Optional.ofNullable(userThreadLocal.get())
                .map(extractor)
                .orElse(new String[0]);
    }

    /**
     * 安全获取 Map<String, GrantedRoles>
     */
    private static Map<Long, ThreadUser.GrantedRoles> safeGetRolesMap() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getRolesMap)
                .orElseGet(() -> {
                    log.warn("获取用户角色失败：当前线程中没有用户信息");
                    return Collections.emptyMap();
                });
    }

    // ====================== 用户基础信息 ======================

    /**
     * 获取用户ID，找不到返回 null
     */
    public static long getUserId() {
        return safeGet(ThreadUser::getUserId, "用户ID");
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
        return safeGet(ThreadUser::getTenantId, "租户id");
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
        return safeGet(ThreadUser::getDeptId, "部门id");
    }

    /**
     * 获取岗位ID
     */
    public static long getPostId() {
        return safeGet(ThreadUser::getPostId,"岗位id");
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
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getRolePermissonMap)
                .orElse(Collections.emptyMap());
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