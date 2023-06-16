package tech.yunyue.core.threaduser;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.exception.BizException;

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
     * 添加当前登录用户方法  在拦截器方法执行前调用设置获取用户
     *
     * @param user
     */
    public static void setCurrentUser(ThreadUser user) {
        userThreadLocal.set(user);
    }

    /**
     * 获取当前登录用户方法
     */
    public static ThreadUser getCurrentUser() {
        return userThreadLocal.get();
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
                .map(ThreadUser::getUserName).orElse(null);
    }

    /**
     * 获取用户名 获取不到抛出异常
     *
     * @return
     */
    public static String getUserNameEx() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getUserName).orElseThrow(() -> new BizException(BizCodeEnum.ACCOUNT_NOT_EXIST));
    }

    /**
     * 获取角色以及角色的数据权限类型
     *
     * @return
     */
    public static Map<String, ThreadUser.GrantedRoles> getUsetGrantedRoles() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(i -> Optional.ofNullable(i.getRolesMap()).orElse(null)).orElse(null);
    }
    /**
     * 获取角色
     *
     * @return
     */
    public static String[] getUserRoles() {
        return Optional.ofNullable(getUsetGrantedRoles())
                .map(i -> i.keySet().stream().toArray(String[]::new)).orElse(null);
    }

    /**
     * 获取角色 获取不到抛出异常
     *
     * @return
     */
    public static String[] getUserRolesEx() {
        return Optional.ofNullable(getUsetGrantedRoles())
                .map(i -> i.keySet().stream().toArray(String[]::new)).orElseThrow(() -> new BizException(BizCodeEnum.ACCOUNT_NOT_EXIST));
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
     * 获取集团租户号 所有用户都有
     *
     * @return
     */
    public static String getTenantId() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getTenantId).orElse(null);
    }

    /**
     * 获取公司租户号  集团用户无
     *
     * @return
     */
    public static String getComTenantId() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getCompanyTenantId).orElse(null);
    }

    /**
     * 获取部门id
     */
    public static String getDeptId() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getDeptId).orElse(null);
    }

    /**
     * 获取岗位id
     */
    public static String getPostId() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getPostId).orElse(null);
    }

    /**
     * 返回当前用户的组织id 即公司用户返回公司id  集团用户返回集团id
     * @return
     */
    public static String getOrgTenantId() {
        return Optional.ofNullable(getComTenantId()).orElse(getTenantId());
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

}
