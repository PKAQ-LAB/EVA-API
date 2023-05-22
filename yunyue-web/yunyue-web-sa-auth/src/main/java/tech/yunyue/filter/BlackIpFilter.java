package tech.yunyue.filter;

import cn.dev33.satoken.util.SaTokenConsts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.util.json.JsonUtil;
import tech.yunyue.core.web.util.RequestUtil;
import tech.yunyue.sys.blacklist.cache.BlackListCacheHelper;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 黑名单过滤器 第一个执行
 */
@Component
@RequiredArgsConstructor
@Order(SaTokenConsts.ASSEMBLY_ORDER-1)
public class BlackIpFilter extends OncePerRequestFilter {
    private final BlackListCacheHelper blackListCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //IP黑名单过滤
        String ip = RequestUtil.getIpAddr(request);
        if(blackListCache.getAll().contains(ip)){
            try (PrintWriter printWriter = response.getWriter()) {
                response.setCharacterEncoding("UTF-8");
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                printWriter.write(JsonUtil.toJson(new Response().failure(BizCodeEnum.BLACK_IP_DENY,ip)));
                printWriter.flush();
            }
            return;
        }

        filterChain.doFilter(request,response);
    }
}
