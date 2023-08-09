package tech.yunyue.filter;

import cn.dev33.satoken.router.SaRouter;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.exception.BizException;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.web.util.RequestUtil;

import java.io.IOException;

/**
 * 校验请求头中是否携带mcode
 */
@Component
@RequiredArgsConstructor
public class McodeFilter extends OncePerRequestFilter {
    @Autowired
    EvaConfig evaConfig;
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var security = evaConfig.getSecurity();
        String[] noMcodePaths = ArrayUtil.addAll(security.getNoMcode(),security.getAnonymous());
        String mcode = RequestUtil.getModuleCode(request);
        if(StrUtil.isEmpty(mcode) && !SaRouter.match(noMcodePaths).isHit()){
            resolver.resolveException(request, response, null, new BizException(BizCodeEnum.MID_DENY));
            return;
        }

        filterChain.doFilter(request,response);
    }
}
