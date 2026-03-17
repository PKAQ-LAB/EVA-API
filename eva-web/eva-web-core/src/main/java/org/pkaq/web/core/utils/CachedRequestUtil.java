package org.pkaq.web.core.utils;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.web.core.filter.CachedBodyHttpServletRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

/**
 * Request 工具类
 *
 * 处理多层包装的 Request，支持重复读取 Body
 *
 * @author PKAQ
 * @date 2026-03-17
 */
@Slf4j
public class CachedRequestUtil {

    private CachedRequestUtil() {
        /* Utility class */
    }

    /**
     * 从请求中读取 Body
     *
     * 自动处理多层包装的情况：
     * 1. CachedBodyHttpServletRequest（优先，自定义实现）
     * 2. ContentCachingRequestWrapper（降级，Spring 内置）
     * 3. 多层嵌套（递归查找）
     *
     * @param request HTTP 请求
     * @return Body 内容，如无法读取返回空字符串
     */
    public static String getRequestBody(HttpServletRequest request) {

        if (request == null) {
            log.warn("Request 为 null");
            return "";
        }

        // 方式 1：优先查找 CachedBodyHttpServletRequest
        CachedBodyHttpServletRequest cachedRequest =
                getNativeRequest(request, CachedBodyHttpServletRequest.class);

        if (cachedRequest != null) {
            String body = cachedRequest.getCachedBodyAsString();
            log.debug("从 CachedBodyHttpServletRequest 读取 Body - 长度: {}", body.length());
            return body;
        }

        // 方式 2：降级到 ContentCachingRequestWrapper
        ContentCachingRequestWrapper wrapper =
                getNativeRequest(request, ContentCachingRequestWrapper.class);

        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();

            if (buf.length > 0) {
                String body = new String(buf, StandardCharsets.UTF_8);
                log.debug("从 ContentCachingRequestWrapper 读取 Body - 长度: {}", body.length());
                return body;
            } else {
                log.trace("ContentCachingRequestWrapper 的缓冲区为空");
            }
        }

        // 未找到缓存
        log.warn("未找到缓存的 Body - 路径: {}, 类型: {}",
                request.getRequestURI(),
                request.getClass().getName());

        return "";
    }

    /**
     * 递归获取指定类型的 Request
     *
     * 处理多层包装的情况：
     * - Spring Security：HeaderWriterRequest
     * - 自定义包装：CachedBodyHttpServletRequest
     * - 其他包装
     *
     * @param request      当前请求
     * @param requiredType 需要的类型
     * @param <T>          返回类型
     * @return 找到的 Request，找不到返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getNativeRequest(HttpServletRequest request, Class<T> requiredType) {

        if (request == null) {
            return null;
        }

        // 检查当前 Request 是否为目标类型
        if (requiredType.isInstance(request)) {
            log.trace("找到目标类型: {}", request.getClass().getName());
            return (T) request;
        }

        // 如果是包装类，递归查找被包装的 Request
        if (request instanceof ServletRequestWrapper) {
            ServletRequestWrapper wrapper = (ServletRequestWrapper) request;
            ServletRequest wrappedRequest = wrapper.getRequest();

            if (wrappedRequest instanceof HttpServletRequest) {
                log.trace("递归查找 - 当前: {}, 被包装: {}",
                        request.getClass().getSimpleName(),
                        wrappedRequest.getClass().getSimpleName());

                return getNativeRequest((HttpServletRequest) wrappedRequest, requiredType);
            }
        }

        log.trace("未找到目标类型: {} - 当前类型: {}",
                requiredType.getSimpleName(),
                request.getClass().getName());

        return null;
    }

    /**
     * 检查请求是否被 CachedBodyHttpServletRequest 包装
     *
     * @param request HTTP 请求
     * @return true：已缓存 false：未缓存
     */
    public static boolean isCachedRequest(HttpServletRequest request) {
        return getNativeRequest(request, CachedBodyHttpServletRequest.class) != null;
    }

    /**
     * 打印 Request 的包装链（用于调试）
     *
     * 示例输出：
     * Request 包装链
     * └─ HeaderWriterRequest
     *   └─ CachedBodyHttpServletRequest
     *     └─ RequestFacade
     *
     * @param request HTTP 请求
     */
    public static void printRequestChain(HttpServletRequest request) {

        if (!log.isDebugEnabled()) {
            return;
        }

        log.debug("Request 包装链");

        HttpServletRequest current = request;
        int level = 0;
        int maxLevel = 10;

        while (current != null && level < maxLevel) {
            String indent = "  ".repeat(level);
            String simpleName = current.getClass().getSimpleName();

            // 高亮 CachedBodyHttpServletRequest
            if (current instanceof CachedBodyHttpServletRequest) {
                log.debug("{}└─ {} -> CachedBody", indent, simpleName);
            } else {
                log.debug("{}└─ {}", indent, simpleName);
            }

            if (current instanceof ServletRequestWrapper) {
                ServletRequestWrapper wrapper = (ServletRequestWrapper) current;
                ServletRequest wrapped = wrapper.getRequest();

                if (wrapped instanceof HttpServletRequest) {
                    current = (HttpServletRequest) wrapped;
                    level++;
                } else {
                    log.debug("{}  └─ {} (非 HttpServletRequest)",
                            indent, wrapped.getClass().getSimpleName());
                    break;
                }
            } else {
                break;
            }
        }

        if (level >= maxLevel) {
            log.warn("Request 包装层级过深 (>{})，可能存在循环", maxLevel);
        }
    }

    /**
     * 获取完整的请求信息（调试用）
     *
     * @param request HTTP 请求
     * @return 请求信息字符串
     */
    public static String getRequestInfo(HttpServletRequest request) {

        if (request == null) {
            return "Request is null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("Request Info:\n");
        sb.append("  Method: ").append(request.getMethod()).append("\n");
        sb.append("  Path: ").append(request.getRequestURI()).append("\n");
        sb.append("  Type: ").append(request.getClass().getName()).append("\n");
        sb.append("  Cached: ").append(isCachedRequest(request)).append("\n");

        String body = getRequestBody(request);
        sb.append("  Body Length: ").append(body.length()).append("\n");

        if (body.length() > 0 && body.length() <= 200) {
            sb.append("  Body Preview: ").append(body).append("\n");
        } else if (body.length() > 200) {
            sb.append("  Body Preview: ").append(body, 0, 200).append("...\n");
        }

        sb.append("========================================");

        return sb.toString();
    }
}
