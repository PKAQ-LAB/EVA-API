package io.nerv.core.threaduser;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.BizException;

import java.util.Optional;

public class ThreadUserHelper {
    /**
     * 保存用户对象的ThreadLocal  在拦截器操作 添加、删除相关用户数据
     */
    private static final ThreadLocal<ThreadUser> userThreadLocal = new TransmittableThreadLocal<>();

    /**
     * 添加当前登录用户方法  在拦截器方法执行前调用设置获取用户
     * @param user
     */
    public static void setCurrentUser(ThreadUser user){
        userThreadLocal.set(user);
    }

    /**
     * 获取当前登录用户方法
     */
    public static ThreadUser getCurrentUser(){
        return userThreadLocal.get();
    }

    /**
     * 删除当前登录用户方法  在拦截器方法执行后 移除当前用户对象
     */
    public static void remove(){
        userThreadLocal.remove();
    }
    /**
     * 获取用户ID
     * @return
     */
    public static String getUserId() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getUserId).orElse(null);
    }
    /**
     * 获取用户ID 获取不到抛出异常
     * @return
     */
    public static String getUserIdEx() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getUserId).orElseThrow(() -> new BizException(BizCodeEnum.ACCOUNT_NOT_EXIST));
    }
    /**
     * 获取用户名
     * @return
     */
    public static String getUserName() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getUserName).orElse(null);
    }
    /**
     * 获取用户名 获取不到抛出异常
     * @return
     */
    public static String getUserNameEx() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getUserName).orElseThrow(() -> new BizException(BizCodeEnum.ACCOUNT_NOT_EXIST));
    }
    /**
     * 获取角色
     * @return
     */
    public static String[] getUserRoles() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getRoles).orElse(null);
    }
    /**
     * 获取角色 获取不到抛出异常
     * @return
     */
    public static String[] getUserRolesEx() {
        return Optional.ofNullable(userThreadLocal.get())
                .map(ThreadUser::getRoles).orElseThrow(() -> new BizException(BizCodeEnum.ACCOUNT_NOT_EXIST));
    }

    /**
     * 是否是管理员
     * 通过判断角色中是否包含管理员角色获得
     * @return
     */
    public static boolean isAdmin() {
      var isAdmin = false;
      var roles = Optional.ofNullable(userThreadLocal.get())
                                        .map(ThreadUser::getRoles).orElse(null);
      if (null != roles){
          isAdmin = ArrayUtil.contains(roles, CommonConstant.ADMIN_ROLE_NAME);
      }
      return isAdmin;
    }

}
