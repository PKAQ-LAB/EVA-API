package org.pkaq.app.doc;

import org.junit.jupiter.api.Test;
import org.pkaq.core.annotation.NoRepeatSubmit;
import org.pkaq.core.cache.store.RedisIdempotencyStore;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.idempotency.IIdempotencyStore;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.util.SecureUtils;
import org.pkaq.web.core.advice.NoRepeatSubmitAdvice;
import org.pkaq.web.core.utils.TokenUtils;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * 验证应用将 Redis 实现装配给中立防重接口，不启动数据库或 Redis。
 *
 * @author PKAQ
 * @date 2026-10-09
 */
class IdempotencyWiringTest {
    /** 通过 Spring 代理实际调用注解入口，重复请求不进入业务方法。 */
    @Test
    void proxyRejectsDuplicateRequestThroughRedisAdapter() {
        new ApplicationContextRunner()
                .withUserConfiguration(StoreConfiguration.class)
                .withPropertyValues("eva.norepeat-check=true")
                .run(context -> {
                    RedisTemplate<Object, Object> redisTemplate = context.getBean(RedisTemplate.class);
                    ValueOperations<Object, Object> values = mock(ValueOperations.class);
                    EvaConfig config = context.getBean(EvaConfig.class);
                    Duration ttl = config.getNorepeatTtl();
                    String key = "eva:idempotency:" + SecureUtils.md5("0:POST:null-/sync-proof");
                    when(redisTemplate.opsForValue()).thenReturn(values);
                    when(values.setIfAbsent(key, Boolean.TRUE, ttl)).thenReturn(true, false);
                    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/sync-proof");
                    request.setServletPath("/sync-proof");
                    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
                    try {
                        RequestProbe probe = context.getBean(RequestProbe.class);
                        assertThat(probe.submit()).isEqualTo(1);
                        assertThrows(BizException.class, probe::submit);
                        assertThat(probe.getCallCount()).isEqualTo(1);
                        verify(values, times(2)).setIfAbsent(key, Boolean.TRUE, ttl);
                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                    }
                });
    }

    /** 开启防重后，切面可从应用上下文获得唯一存储实现。 */
    @Test
    void wiresRedisAdapterWithoutConnectingInfrastructure() {
        new ApplicationContextRunner()
                .withUserConfiguration(StoreConfiguration.class)
                .withPropertyValues("eva.norepeat-check=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(IIdempotencyStore.class);
                    assertThat(context).hasSingleBean(NoRepeatSubmitAdvice.class);
                    assertThat(context.getBean(IIdempotencyStore.class)).isInstanceOf(RedisIdempotencyStore.class);
                    verify(context.getBean(RedisTemplate.class), never()).opsForValue();
                });
    }

    /** 隔离装配真实切面与适配器，仅替换底层 Redis 客户端。 */
    @Configuration(proxyBeanMethods = false)
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @Import({RedisIdempotencyStore.class, NoRepeatSubmitAdvice.class, TokenUtils.class})
    static class StoreConfiguration {
        /** 提供可由切面拦截的隔离业务入口。 */
        @Bean
        RequestProbe requestProbe() {
            return new RequestProbe();
        }

        /** 提供本测试的默认配置。 */
        @Bean
        EvaConfig evaConfig() {
            return new EvaConfig();
        }

        /** 提供不会发起网络连接的 Redis 客户端。 */
        @Bean
        RedisTemplate<Object, Object> redisTemplate() {
            return mock(RedisTemplate.class);
        }
    }

    /**
     * 统计真实经过代理的业务调用次数。
     *
     * @author PKAQ
     * @date 2026-10-09
     */
    static class RequestProbe {
        private int callCount;

        /** 返回业务执行次数，仅首次请求允许进入。 */
        @NoRepeatSubmit
        public int submit() {
            return ++this.callCount;
        }

        /** 返回执行次数，用于确认重复请求被拦截。 */
        public int getCallCount() {
            return this.callCount;
        }
    }
}
