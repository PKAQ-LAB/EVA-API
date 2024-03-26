package tech.yunyue.core.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.threaduser.ThreadUserHelper;

import java.io.IOException;

/**
 * 过滤器：清除 ThreadLocal 中的安全上下文信息
 * 该过滤器用于确保在每个请求处理结束后清除 ThreadLocal 中的安全上下文信息，
 * 防止安全上下文信息的泄漏或重用导致的安全问题。
 * <p>
 * 在请求处理完成后，使用 ThreadUserHelper.remove() 方法清除 ThreadLocal 中的安全上下文信息。
 *
 * @author mja
 */
@Component
@Order(CommonConstant.ASSEMBLY_ORDER - 100)
public class ThreadLocalSecurityCleanupFilter extends OncePerRequestFilter {

    /**
     * 过滤器链中的实际处理方法，在请求处理完成后清除 ThreadLocal 中的安全上下文信息。
     *
     * @param request  HttpServletRequest 对象，表示客户端的请求。
     * @param response HttpServletResponse 对象，表示服务器对客户端请求的响应。
     * @param chain    FilterChain 对象，用于调用过滤器链中的下一个过滤器。
     * @throws ServletException 如果发生 Servlet 异常。
     * @throws IOException      如果发生 IO 异常。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } finally {
            // 清除 ThreadLocal 中的安全上下文信息
            ThreadUserHelper.remove();
        }
    }
}
