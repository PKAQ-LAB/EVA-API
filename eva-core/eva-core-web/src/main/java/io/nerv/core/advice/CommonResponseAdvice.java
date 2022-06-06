package io.nerv.core.advice;

import io.nerv.core.enums.BizCode;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.vo.Response;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一处理返回值
 * 注意 这里只对io.nerv包下的返回值自动响应处理
 */
@RestControllerAdvice(basePackages = "io.nerv")
public class CommonResponseAdvice implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {


        if (body instanceof Response){
            return body;
        }
        Response res = new Response();
                 res.setSuccess(true);

        if (body instanceof BizCode){
            res.setCode(((BizCode) body).getIndex());
            res.setMessage(((BizCode) body).getName());
        } else {
            res.setMessage(BizCodeEnum.OPERATE_SUCCESS.getIndex());
            res.setData(body);
        }

        return res;
    }
}
