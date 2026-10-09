package org.pkaq.web.core.advice;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.idempotency.IIdempotencyStore;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.SecureUtils;
import org.pkaq.web.core.utils.TokenUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 防重切面只依赖中立接口的隔离测试，不连接 Redis。
 *
 * @author PKAQ
 * @date 2026-10-09
 */
class NoRepeatSubmitAdviceTest {
    private final IIdempotencyStore store = mock(IIdempotencyStore.class);
    private final TokenUtils tokens = mock(TokenUtils.class);
    private final EvaConfig config = new EvaConfig();
    private final ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    private final NoRepeatSubmitAdvice advice = new NoRepeatSubmitAdvice(store, tokens, config);

    /** 绑定模拟请求，固定方法、Token 与路由以核对原有键算法。 */
    @BeforeEach
    void bindRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/sys/user");
        request.setServletPath("/sys/user");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(tokens.getToken(request)).thenReturn("test-token");
    }

    /** 释放请求上下文，不影响后续测试。 */
    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    /** 首次请求保持原有哈希键、有效期并继续业务执行。 */
    @Test
    void firstRequestPreservesKeyTtlAndResult() throws Throwable {
        when(store.acquire(anyString(), any())).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("completed");

        assertEquals("completed", advice.arround(joinPoint));

        verify(store).acquire(SecureUtils.md5("0:POST:test-token-/sys/user"), config.getNorepeatTtl());
    }

    /** 同一时间窗内重复请求仍拒绝且不调用业务方法。 */
    @Test
    void duplicateRequestDoesNotProceed() throws Throwable {
        BizException failure = assertThrows(BizException.class, () -> advice.arround(joinPoint));

        assertEquals(CommonCodes.REQUEST_TOO_MORE, failure.getBizCode());
        verify(joinPoint, never()).proceed();
    }

    /** 存储异常维持原有防重失败行为，不绕过保护。 */
    @Test
    void storageFailureDoesNotProceed() throws Throwable {
        when(store.acquire(anyString(), any())).thenThrow(new IllegalStateException("storage unavailable"));

        BizException failure = assertThrows(BizException.class, () -> advice.arround(joinPoint));

        assertEquals(CommonCodes.REQUEST_TOO_MORE, failure.getBizCode());
        verify(joinPoint, never()).proceed();
    }

    /** 当前租户标识仍参与原有防重哈希键。 */
    @Test
    void tenantIdentityRemainsInRequestKey() {
        ThreadUser user = new ThreadUser();
        user.setTenantId(17L);
        when(store.acquire(anyString(), any())).thenReturn(true);

        ThreadUserHelper.runWithUser(user, () -> advice.arround(joinPoint));

        verify(store).acquire(SecureUtils.md5("17:POST:test-token-/sys/user"), config.getNorepeatTtl());
    }
}
