package io.nerv.config;

import io.nerv.security.interceptor.UserInfoInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfiguer implements WebMvcConfigurer {

    @Autowired
    private UserInfoInterceptor userInfoInterceptor;
    /**
     * 拦截器 管理 threadlocal中的用户信息
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.userInfoInterceptor).addPathPatterns("/**");
    }
}
