package org.pkaq.web.core.advice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.cache.store.RedisIdempotencyStore;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.SecureUtils;
import org.pkaq.web.core.utils.TokenUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * 防止重复提交切入点
 *
 * @author PKAQ
 */
@Aspect
@Component
@Slf4j

@ConditionalOnProperty(prefix = "eva", name = "norepeat-check", havingValue = "true")
@RequiredArgsConstructor
public class NoRepeatSubmitAdvice {

    private final RedisIdempotencyStore idempotencyStore;

    private final TokenUtils tokenUtil;

    private final EvaConfig evaConfig;

    @Around("@annotation(org.pkaq.core.annotation.NoRepeatSubmit)")
    public Object arround(ProceedingJoinPoint pjp) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            // 请求类型
            String method = request.getMethod();
            ThreadUser currentUser = ThreadUserHelper.getCurrentUserOrNull();
            long tenantId = currentUser == null ? 0L : currentUser.getTenantId();
            var key = tenantId + ":" + method + ":" + tokenUtil.getToken(request)
                    + "-" + request.getServletPath();
            key = SecureUtils.md5(key);

            // Redis 原子写入失败表示同一窗口内已经存在相同请求。
            if (!idempotencyStore.acquire(key, evaConfig.getNorepeatTtl())) {
                throw new BizException(CommonCodes.REQUEST_TOO_MORE);
            }
            return pjp.proceed();
        } catch (BizException exception) {
            throw exception;
        } catch (Throwable e) {
            log.error("验证重复提交时出现未知异常!", e);
            throw new BizException(CommonCodes.REQUEST_TOO_MORE);
        }

    }
}
