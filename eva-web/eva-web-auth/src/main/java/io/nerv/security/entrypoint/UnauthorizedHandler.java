package io.nerv.security.entrypoint;

import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.util.JsonUtil;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

/**
 * 鉴权失败时的响应
 * @author: S.PKAQ
 * @Datetime: 2018/4/24 23:24
 */
@Component
public class UnauthorizedHandler implements AuthenticationEntryPoint, Serializable {
    private final JsonUtil jsonUtil;

    public UnauthorizedHandler(JsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    //当访问的资源没有权限，会调用这里
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        //返回json形式的错误信息

        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);

        try(PrintWriter printWriter = response.getWriter()){
            printWriter.write(jsonUtil.toJSONString(
                    new Response()
                            .failure(BizCodeEnum.PERMISSION_DENY)));
            printWriter.flush();
        }
    }
}
