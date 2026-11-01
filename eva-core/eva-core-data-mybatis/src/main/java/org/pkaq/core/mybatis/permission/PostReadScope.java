package org.pkaq.core.mybatis.permission;

import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 指定岗位仅在当前主体的同步纯读取调用中贡献范围。
 * @author PKAQ
 * @date 2026-11-01
 */
public final class PostReadScope {
    private static final ThreadLocal<Subject> READ = new ThreadLocal<>();
    private static final ThreadLocal<String> SELECT = new ThreadLocal<>();

    private PostReadScope() {
    }

    /** 由明确登记的控制器方法打开，异步线程不继承。 */
    public static Window read() {
        Subject previous = READ.get();
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();
        READ.set(user == null ? null : new Subject(user, user.getUserId(), user.getTenantId()));
        return () -> restore(READ, previous);
    }

    /** 仅由实际SELECT语句的拦截入口打开。 */
    public static Window select(String statement) {
        String previous = SELECT.get();
        SELECT.set(statement);
        return () -> restore(SELECT, previous);
    }

    /** 三条件及主体身份必须同时匹配，方法名或HTTP动作不作为依据。 */
    public static boolean allows(String statement) {
        Subject subject = READ.get();
        ThreadUser user = ThreadUserHelper.getCurrentUserOrNull();
        return subject != null && user == subject.user() && user.getUserId() == subject.id()
                && user.getTenantId() == subject.tenant() && statement != null && statement.equals(SELECT.get())
                && TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }

    private static <T> void restore(ThreadLocal<T> slot, T previous) {
        if (previous == null) {
            slot.remove();
        } else {
            slot.set(previous);
        }
    }

    /** 窗口退出不引入受检异常。 */
    public interface Window extends AutoCloseable {
        @Override
        void close();
    }

    private record Subject(ThreadUser user, long id, long tenant) {
    }
}
