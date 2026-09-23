package org.pkaq.web.core.client;

import org.junit.jupiter.api.Test;
import org.pkaq.core.properties.ClientInfoProperties;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * 客户端信息解析测试。
 *
 * @author PKAQ
 */
class ClientInfoResolverTest {

    /**
     * 非可信来源不得伪造转发IP。
     */
    @Test
    void shouldIgnoreForwardedIpFromUntrustedRemoteAddress() {
        EvaConfig evaConfig = new EvaConfig();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.10");
        request.addHeader("X-Forwarded-For", "198.51.100.20");

        ClientInfo clientInfo = new ClientInfoResolver(evaConfig).resolve(request);

        assertEquals("203.0.113.10", clientInfo.ip());
    }

    /**
     * 可信代理链应返回最接近代理边界的非可信客户端地址。
     */
    @Test
    void shouldResolveClientIpThroughTrustedProxyChain() {
        EvaConfig evaConfig = new EvaConfig();
        ClientInfoProperties properties = new ClientInfoProperties();
        properties.setTrustedProxies(List.of("10.0.0.0/8", "192.168.0.0/16"));
        evaConfig.setClientInfo(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.2");
        request.addHeader("X-Forwarded-For", "198.51.100.20, 192.168.1.3");

        ClientInfo clientInfo = new ClientInfoResolver(evaConfig).resolve(request);

        assertEquals("198.51.100.20", clientInfo.ip());
    }

    /**
     * 常见浏览器User-Agent应解析设备、系统、浏览器和稳定摘要。
     */
    @Test
    void shouldParseCommonBrowserUserAgent() {
        EvaConfig evaConfig = new EvaConfig();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0");

        ClientInfo first = new ClientInfoResolver(evaConfig).resolve(request);
        ClientInfo second = new ClientInfoResolver(evaConfig).resolve(request);

        assertEquals("DESKTOP", first.deviceType());
        assertEquals("Windows", first.osName());
        assertEquals("Edge", first.browserName());
        assertNotEquals("UNKNOWN", first.fingerprint());
        assertEquals(first.fingerprint(), second.fingerprint());
    }
}
