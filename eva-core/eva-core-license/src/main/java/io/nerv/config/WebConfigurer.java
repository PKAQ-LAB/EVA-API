package io.nerv.config;

import io.nerv.core.license.LicenseCheckInterceptor;
import io.nerv.core.properties.EvaConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.Charset;

/**
 * @author PKAQ
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "eva.license", name = "enable", havingValue = "true")
public class WebConfigurer implements WebMvcConfigurer {

    private final LicenseCheckInterceptor licenseCheckInterceptor;

    private final EvaConfig evaConfig;

    /**
     * 配置license鉴权拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(licenseCheckInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(evaConfig.getSecurity().getPermit());
    }

    // 解决乱码问题 StringHttpMessageConverter默认编码为ISO-8859-1
    @Bean
    public HttpMessageConverter<String> responseBodyConverter() {
        return new StringHttpMessageConverter(Charset.forName("UTF-8"));
    }
}
