package io.nerv.core.license;

import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.response.Response;
import io.nerv.core.util.json.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import java.io.PrintWriter;

/**
 * license 验证拦截器
 *
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "eva.license", name = "enable", havingValue = "true")
public class LicenseCheckInterceptor implements AsyncHandlerInterceptor {

    private final LicenseVerify licenseVerify;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //校验证书是否有效
        boolean verifyResult = licenseVerify.vertify();

        if (verifyResult) {
            return true;
        } else {
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_OK);

            try (PrintWriter printWriter = response.getWriter()) {
                printWriter.write(JsonUtil.toJson(
                        new Response()
                                .failure(BizCodeEnum.LICENSE_LICENSEHASEXPIRED)));
                printWriter.flush();
            }
            return false;
        }
    }

}
