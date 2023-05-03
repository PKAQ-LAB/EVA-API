package tech.yunyue.core.web.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.properties.EvaConfig;



@Component
@RequiredArgsConstructor
public class TenantUtil {
    private final EvaConfig evaConfig;

    /**
     * 获取Tenant Id
     *
     * @param request
     * @return
     */
    public String getTenantId(HttpServletRequest request) {
        String tenantId = null;

        var authHeader = request.getHeader(evaConfig.getTenant().getHeader());

        var cookie = JakartaServletUtil.getCookie(request, CommonConstant.tenantId);
        if (null != cookie) {
            tenantId = cookie.getValue();
        } else if (StrUtil.isNotBlank(authHeader)) {
            tenantId = authHeader;
        }

        return tenantId;
    }
}
