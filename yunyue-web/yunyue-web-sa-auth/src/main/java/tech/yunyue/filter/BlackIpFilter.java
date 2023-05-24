package tech.yunyue.filter;

import cn.dev33.satoken.util.SaTokenConsts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.exception.BizException;
import tech.yunyue.core.web.util.RequestUtil;
import tech.yunyue.sys.blacklist.cache.BlackListCacheHelper;

import java.io.IOException;

/**
 * 黑名单过滤器 第一个执行
 */
@Component
@RequiredArgsConstructor
@Order(SaTokenConsts.ASSEMBLY_ORDER-1)
public class BlackIpFilter extends OncePerRequestFilter {
    private final BlackListCacheHelper blackListCache;
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //IP黑名单过滤
        String ip = RequestUtil.getIpAddr(request);
        if(blackListCache.getAll().contains(ip)){
            resolver.resolveException(request, response, null, new BizException(BizCodeEnum.BLACK_IP_DENY,ip));
            return;
        }

        filterChain.doFilter(request,response);
    }
}
