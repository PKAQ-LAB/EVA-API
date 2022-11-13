package io.nerv.auth.config;

import io.nerv.auth.security.interceptor.UserInfoInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author PKAQ
 */
@Configuration
@RequiredArgsConstructor
public class MvcConfiguer implements WebMvcConfigurer {

    private final UserInfoInterceptor userInfoInterceptor;
    /**
     * 拦截器 管理 threadlocal中的用户信息
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.userInfoInterceptor).addPathPatterns("/**");
    }
}
