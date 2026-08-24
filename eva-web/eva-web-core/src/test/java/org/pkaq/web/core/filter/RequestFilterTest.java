package org.pkaq.web.core.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.properties.Cloud;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 请求来源过滤器测试。
 *
 * @author PKAQ
 */
class RequestFilterTest {

    /**
     * 验证默认关闭网关请求校验。
     *
     * @throws Exception 过滤器执行异常
     */
    @Test
    void shouldAllowRequestWhenCloudCheckIsDisabled() throws Exception {
        EvaConfig evaConfig = new EvaConfig();
        RequestFilter filter = new RequestFilter(evaConfig);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    /**
     * 验证开启校验后拒绝未经过网关的请求。
     *
     * @throws Exception 过滤器执行异常
     */
    @Test
    void shouldRejectRequestWithoutGatewayHeaderWhenEnabled() throws Exception {
        RequestFilter filter = new RequestFilter(this.enabledConfig());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/sys/user/list");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(403, response.getStatus());
    }

    /**
     * 验证开启校验后允许网关转发的请求。
     *
     * @throws Exception 过滤器执行异常
     */
    @Test
    void shouldAllowGatewayRequestWhenEnabled() throws Exception {
        RequestFilter filter = new RequestFilter(this.enabledConfig());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CommonConstant.X_GATEWAY_HEADER, CommonConstant.X_GATEWAY_VALUE);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    /**
     * 验证自定义网关请求头名称和值能够生效。
     *
     * @throws Exception 过滤器执行异常
     */
    @Test
    void shouldAllowConfiguredGatewayHeader() throws Exception {
        EvaConfig evaConfig = this.enabledConfig();
        evaConfig.getCloud().setRequestHeader("X-Internal-Gateway");
        evaConfig.getCloud().setRequestValue("custom-gateway-request");
        RequestFilter filter = new RequestFilter(evaConfig);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Internal-Gateway", "custom-gateway-request");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    private EvaConfig enabledConfig() {
        Cloud cloud = new Cloud();
        cloud.setEnable(true);
        EvaConfig evaConfig = new EvaConfig();
        evaConfig.setCloud(cloud);
        return evaConfig;
    }
}
