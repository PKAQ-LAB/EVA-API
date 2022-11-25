package io.nerv.config;

import io.nerv.core.web.interceptor.UserInfoInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author PKAQ
 */
@Configuration
@RequiredArgsConstructor
public class MvcConfiguration implements WebMvcConfigurer {

    private final UserInfoInterceptor userInfoInterceptor;

    /**
     * 拦截器 管理 threadlocal中的用户信息
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.userInfoInterceptor).addPathPatterns("/**");
    }
}
