package org.pkaq.core.mybatis.permission;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 岗位读取窗口只包裹已登记控制器，服务事务仍由原代理管理。
 * @author PKAQ
 * @date 2026-11-01
 */
@Aspect
@Component
public class PostScopeReadAspect {
    /** 退出时恢复上层主体窗口，异常路径也不保留读取用途。 */
    @Around("@annotation(org.pkaq.core.mybatis.permission.PostScopeRead)"
            + " && @within(org.springframework.web.bind.annotation.RestController)")
    public Object read(ProceedingJoinPoint invocation) throws Throwable {
        try (PostReadScope.Window ignored = PostReadScope.read()) {
            return invocation.proceed();
        }
    }
}
