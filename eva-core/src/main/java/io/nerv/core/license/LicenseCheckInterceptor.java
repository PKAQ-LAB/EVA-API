package io.nerv.core.license;

import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * license 验证拦截器
 */
@Component
@ConditionalOnProperty(prefix = "eva.license", name = "enable", havingValue = "true")
public class LicenseCheckInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private JsonUtil jsonUtil;

    @Autowired
    private LicenseVerify licenseVerify;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LicenseVerify licenseVerify = new LicenseVerify();

        //校验证书是否有效
        boolean verifyResult = licenseVerify.vertify();

        if(verifyResult){
            return true;
        }else{
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_OK);

            try(PrintWriter printWriter = response.getWriter()){
                printWriter.write(jsonUtil.toJSONString(
                        new Response()
                                .failure(BizCodeEnum.LICENSE_LICENSEHASEXPIRED)));
                printWriter.flush();
            }
            return false;
        }
    }

}
