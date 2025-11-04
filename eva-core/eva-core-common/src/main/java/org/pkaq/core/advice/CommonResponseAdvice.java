package org.pkaq.core.advice;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.BizCode;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.i18n.I18NHelper;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.util.StrUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * 统一处理返回值
 *
 * @author PKAQ
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class CommonResponseAdvice implements ResponseBodyAdvice {
    private final I18NHelper i18NHelper;

    @Value("${project.version}")
    private String version;

    /**
     * 判断是否要执行 beforeBodyWrite 方法，true为执行，false不执行，有注解标记的时候处理返回值
     * 这里整合swagger出现了问题，swagger相关的不拦截
     */
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return !returnType.getDeclaringClass().getName().contains("OpenApi") &&
                !returnType.getDeclaringClass().getName().contains("Swagger") &&
                !returnType.getDeclaringClass().getName().contains("swagger");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        response.getHeaders().set("version", StrUtils.isNotEmpty(version) ? version : "unknown");

        var res = new Response<>();

        // 返回的是BizCode类型
        if (body instanceof BizCode biz) {
            res.setCode(biz.getCode());

            var msg = i18NHelper.getMsg(biz);
            res.setMessage(msg);
            return res;
        }

        // 自定义response返回类型
        if (body instanceof Response) {
            res = (Response) body;

            var mtype = res.getMtype();

            String code = StrUtils.defaultIfBlank(res.getCode(), "");
            String message = StrUtils.defaultIfBlank(res.getMessage(), "");

            if (mtype instanceof BizCode biz) {
                // 非国际化类型提示
                code = biz.getCode();
                message = biz.getMsg();

            } else if (message.startsWith("{") && message.endsWith("}")) {
                // 花括号包裹的
                code = message.substring(1, message.length() - 1);
                message = code;
            }

            message = i18NHelper.getMessage(code, res.getArgs(), message);

            message = MessageFormat.format(message, res.getArgs());

            res.setCode(code);
            res.setMessage(message);

        } else {
            // 只处理继承Ctrl类的响应，第三方接口不需要处理
            if (!Objects.equals(Ctrl.class, returnType.getMethod().getDeclaringClass().getSuperclass())) {
                return body;
            }

            var msg = i18NHelper.getMsg(CommonCodes.OPERATE_SUCCESS);
            res.setCode(CommonCodes.OPERATE_SUCCESS.getCode());
            res.setMessage(msg);
            res.setData(body);
        }

        // 如果是成功
        if (res.isSuccess()) res.setCode("0000");

        return res;
    }
}
