package org.pkaq.core.auth.openapi.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.auth.AuthCodes;
import org.pkaq.core.auth.openapi.consts.OpenApiConsts;
import org.pkaq.core.auth.openapi.entity.AppCredentialEntity;
import org.pkaq.core.auth.openapi.exception.AppKeyAuthenticationException;
import org.pkaq.core.auth.openapi.log.service.OpenApiCallLogService;
import org.pkaq.core.auth.openapi.security.SignatureValidator;
import org.pkaq.core.auth.openapi.service.AppKeyService;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.util.json.JsonUtil;
import org.pkaq.web.core.filter.CachedBodyHttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * AppKey认证过滤器
 *
 * @author PKAQ
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class AppKeyAuthenticationFilter extends OncePerRequestFilter {

    private final AppKeyService appKeyService;
    private final SignatureValidator signatureValidator;
    private final OpenApiCallLogService openApiCallLogService;
    private final EvaConfig evaConfig;

    /**
     * 递归获取指定类型的请求包装
     */
    public static <T> T getNativeRequest(HttpServletRequest request, Class<T> requiredType) {
        if (requiredType.isInstance(request)) {
            return requiredType.cast(request);
        }

        if (request instanceof ServletRequestWrapper) {
            ServletRequestWrapper wrapper = (ServletRequestWrapper) request;
            ServletRequest wrapped = wrapper.getRequest();

            if (wrapped instanceof HttpServletRequest) {
                return getNativeRequest((HttpServletRequest) wrapped, requiredType);
            }
        }

        return null;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestPath = resolveRequestPath(request);

        var openApiConfig = evaConfig.getAuth().getOpenApi();
        var headerConfig = openApiConfig.getHeaders();
        String appKey = request.getHeader(headerConfig.getAppKey());
        String timestampStr = request.getHeader(headerConfig.getTimestamp());
        String signature = request.getHeader(headerConfig.getSignature());

        log.debug("处理请求 - appKey: {}, 路径: {}, 时间戳: {}", appKey, requestPath, timestampStr);

        if (appKey == null || timestampStr == null || signature == null) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.nanoTime();
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            log.warn("无效的时间戳格式: {}", timestampStr);
            this.openApiCallLogService.save(request, null, requestPath, startTime,
                    HttpServletResponse.SC_UNAUTHORIZED, "无效的时间戳格式");
            AuthCodes.OPENAPI_INVALID_TIMESTAMP_FORMAT.newException();
            return;
        }

        AppCredentialEntity credential = null;
        try {
            String body = "";
            CachedBodyHttpServletRequest wrapper = getNativeRequest(request, CachedBodyHttpServletRequest.class);
            if (wrapper != null) {
                byte[] buf = wrapper.getCachedBody();
                body = new String(buf, StandardCharsets.UTF_8);
            }

            body = JsonUtil.normalizeJsonBody(body);
            request.setAttribute(OpenApiConsts.REQUEST_BODY, body);

            credential = appKeyService.getCredential(appKey);
            if (credential == null) {
                log.warn("AppKey未找到: {}", appKey);
                AuthCodes.OPENAPI_APP_KEY_NOT_FOUND.newException();
            }

            if (!credential.isValid()) {
                log.warn("无效或过期的AppKey: {}, 状态: {}, 过期时间: {}",
                        appKey, credential.getStatus(), credential.getExpireTime());
                AuthCodes.OPENAPI_INVALID_OR_EXPIRED_APP_KEY.newException();
            }

            boolean validSignature = signatureValidator.validateSignature(
                    appKey,
                    credential.getAppSecret(),
                    timestamp,
                    requestPath,
                    body,
                    signature
            );

            if (!validSignature) {
                log.warn("无效的签名 - AppKey: {}, 路径: {}", appKey, requestPath);
                AuthCodes.OPENAPI_INVALID_SIGNATURE.newException();
            }

            if (!credential.hasApiPermission(requestPath)) {
                log.warn("AppKey无权限访问 - AppKey: {}, 路径: {}", appKey, requestPath);
                AuthCodes.OPENAPI_NO_PERMISSION.newException();
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    credential,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_USER"))
            );
            authentication.setDetails(credential);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("认证成功 - appKey: {}, 应用: {}, 路径: {}", appKey, credential.getAppName(), requestPath);

            filterChain.doFilter(request, response);
            this.openApiCallLogService.save(request, credential, requestPath, startTime, response.getStatus(), null);

        } catch (AppKeyAuthenticationException e) {
            log.error("认证失败: {}", e.getMessage());
            this.openApiCallLogService.save(request, credential, requestPath, startTime,
                    HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            AuthCodes.OPENAPI_AUTHENTICATION_FAILED.newException();
        } catch (Exception e) {
            log.error("认证异常错误", e);
            this.openApiCallLogService.save(request, credential, requestPath, startTime,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            AuthCodes.OPENAPI_UNEXPECTED_AUTH_ERROR.newException();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = resolveRequestPath(request);
        if (!evaConfig.getAuth().matchOpenApiPath(path)) {
            return true;
        }

        return path.startsWith("/actuator/health")
                || path.startsWith("/actuator/prometheus")
                || path.startsWith("/actuator/info")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/doc.html")
                || path.startsWith("/webjars")
                || path.equals("/favicon.ico");
    }

    /**
     * 获取去除 context-path 后的请求路径。
     */
    private String resolveRequestPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (servletPath != null && !servletPath.isBlank()) {
            return servletPath;
        }
        return request.getRequestURI();
    }
}



