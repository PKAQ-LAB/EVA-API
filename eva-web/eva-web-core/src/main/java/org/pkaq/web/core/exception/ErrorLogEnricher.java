package org.pkaq.web.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.advice.ExceptionInfo;
import org.pkaq.web.core.utils.IpUtils;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 在异常事件到达持久化监听器之前，补充 IP 和请求参数
 *
 * @author PKAQ
 */
@Slf4j
@Component
public class ErrorLogEnricher {

    @EventListener
    @Order(0)
    public void enrich(ExceptionInfo info) {
        try {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                HttpServletRequest request = sra.getRequest();

                info.setIp(IpUtils.getIPAddress(request));

                String query = request.getQueryString();
                info.setParams("%s %s%s".formatted(
                        request.getMethod(),
                        request.getRequestURI(),
                        query != null ? "?" + query : ""
                ));
            }
        } catch (Exception ignored) {
            // 补充信息失败不影响日志记录
        }
    }
}
